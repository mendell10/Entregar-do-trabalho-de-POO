package escola;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Testes automáticos das regras da escola, sem biblioteca externa.
 * Rodar: java -cp out escola.TestesEscola   (retorna código 1 se algum teste falhar)
 */
public class TestesEscola {
    private static int passaram = 0;
    private static int falharam = 0;

    private static final LocalDate HOJE = LocalDate.of(2026, 11, 23);

    // ---- mini framework ----
    @FunctionalInterface
    private interface Corpo { void rodar() throws Exception; }

    private static void teste(String nome, Corpo corpo) {
        try {
            corpo.rodar();
            passaram++;
            System.out.println("  OK    " + nome);
        } catch (Exception | AssertionError e) {
            falharam++;
            System.out.println("  FALHA " + nome + " -> " + e);
        }
    }

    private static void igual(Object esperado, Object atual) {
        if (!esperado.equals(atual)) throw new AssertionError("esperado <" + esperado + "> mas veio <" + atual + ">");
    }

    private static void verdade(boolean condicao, String msg) {
        if (!condicao) throw new AssertionError(msg);
    }

    private static void bloqueia(String trechoDaMensagem, Runnable acao) {
        try {
            acao.run();
        } catch (RegraDeNegocioException e) {
            verdade(e.getMessage().contains(trechoDaMensagem),
                    "mensagem inesperada: \"" + e.getMessage() + "\" (esperava conter \"" + trechoDaMensagem + "\")");
            return;
        }
        throw new AssertionError("deveria ter sido bloqueado (" + trechoDaMensagem + ")");
    }

    private static Horario seg(int de, int ate) {
        return new Horario(DayOfWeek.MONDAY, LocalTime.of(de, 0), LocalTime.of(ate, 0));
    }

    /** Escola pronta: 3 cursos, 2 professores, 3 salas, Maria VIP (2 anos), João e Carlos. */
    private static class Cenario {
        final Escola escola = new Escola("Teste");
        final Curso piano = escola.cadastrarCurso("Piano");
        final Curso violao = escola.cadastrarCurso("Violão");
        final Curso canto = escola.cadastrarCurso("Canto");
        final Professor ana = escola.cadastrarProfessor("Ana", Set.of(piano, canto));
        final Professor bruno = escola.cadastrarProfessor("Bruno", Set.of(violao));
        final Sala aula1 = escola.cadastrarSala("Sala 1", TipoSala.AULA);
        final Sala aula2 = escola.cadastrarSala("Sala 2", TipoSala.AULA);
        final Sala pratica = escola.cadastrarSala("Prática A", TipoSala.PRATICA);
        final Sala estudio = escola.cadastrarSala("Estúdio", TipoSala.ESTUDIO);
        final Aluno maria = escola.cadastrarAluno("Maria", true, HOJE.minusYears(2).minusDays(10));
        final Aluno joao = escola.cadastrarAluno("João", false, HOJE.minusMonths(3));
        final Aluno carlos = escola.cadastrarAluno("Carlos", false, HOJE);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("== Turmas e horários ==");
        teste("valores dos tipos de aula", () -> {
            igual(Dinheiro.de("280.00"), TipoAula.INDIVIDUAL.getMensalidade());
            igual(Dinheiro.de("160.00"), TipoAula.EM_GRUPO.getMensalidade());
            igual(1, TipoAula.INDIVIDUAL.getLimiteAlunos());
            igual(6, TipoAula.EM_GRUPO.getLimiteAlunos());
        });
        teste("professor não pode ter dois compromissos no mesmo horário", () -> {
            Cenario c = new Cenario();
            c.escola.criarTurma(c.piano, TipoAula.INDIVIDUAL, c.ana, c.aula1, seg(14, 15));
            bloqueia("Ana já tem aula", () -> c.escola.criarTurma(c.canto, TipoAula.EM_GRUPO, c.ana, c.aula2, seg(14, 16)));
        });
        teste("sala não pode ter dois compromissos no mesmo horário", () -> {
            Cenario c = new Cenario();
            c.escola.criarTurma(c.piano, TipoAula.INDIVIDUAL, c.ana, c.aula1, seg(14, 15));
            bloqueia("Sala 1 já está ocupada", () -> c.escola.criarTurma(c.violao, TipoAula.EM_GRUPO, c.bruno, c.aula1, seg(14, 15)));
        });
        teste("horários que só se encostam não conflitam", () -> {
            Cenario c = new Cenario();
            c.escola.criarTurma(c.piano, TipoAula.INDIVIDUAL, c.ana, c.aula1, seg(14, 15));
            c.escola.criarTurma(c.canto, TipoAula.EM_GRUPO, c.ana, c.aula1, seg(15, 16));
        });
        teste("professor só leciona o que está habilitado", () -> {
            Cenario c = new Cenario();
            bloqueia("não leciona", () -> c.escola.criarTurma(c.violao, TipoAula.EM_GRUPO, c.ana, c.aula1, seg(10, 11)));
        });
        teste("sala de prática/estúdio não serve para aula", () -> {
            Cenario c = new Cenario();
            bloqueia("não é uma sala de aula", () -> c.escola.criarTurma(c.piano, TipoAula.INDIVIDUAL, c.ana, c.pratica, seg(10, 11)));
        });
        teste("turma individual aceita 1 aluno; em grupo aceita 6 e bloqueia o 7º", () -> {
            Cenario c = new Cenario();
            Turma ind = c.escola.criarTurma(c.piano, TipoAula.INDIVIDUAL, c.ana, c.aula1, seg(8, 9));
            c.escola.matricular(c.maria, ind);
            bloqueia("lotada", () -> c.escola.matricular(c.joao, ind));
            Turma grupo = c.escola.criarTurma(c.violao, TipoAula.EM_GRUPO, c.bruno, c.aula2, seg(10, 11));
            for (int i = 1; i <= 6; i++) c.escola.matricular(c.escola.cadastrarAluno("Aluno" + i, false, HOJE), grupo);
            bloqueia("lotada", () -> c.escola.matricular(c.joao, grupo));
        });
        teste("aluno não pode estar em duas turmas no mesmo horário", () -> {
            Cenario c = new Cenario();
            Turma t1 = c.escola.criarTurma(c.piano, TipoAula.INDIVIDUAL, c.ana, c.aula1, seg(14, 15));
            Turma t2 = c.escola.criarTurma(c.violao, TipoAula.EM_GRUPO, c.bruno, c.aula2, seg(14, 15));
            c.escola.matricular(c.maria, t1);
            bloqueia("Maria já tem aula", () -> c.escola.matricular(c.maria, t2));
        });

        System.out.println("== Mensalidade e fidelidade ==");
        teste("fidelidade: 5% por ano completo, máximo 15%", () -> {
            LocalDate hoje = LocalDate.of(2026, 11, 23);
            igual(0, PlanoFidelidade.percentualDesconto(hoje.minusMonths(11), hoje));
            igual(5, PlanoFidelidade.percentualDesconto(hoje.minusYears(1), hoje));
            igual(10, PlanoFidelidade.percentualDesconto(hoje.minusYears(2).minusDays(1), hoje));
            igual(15, PlanoFidelidade.percentualDesconto(hoje.minusYears(3), hoje));
            igual(15, PlanoFidelidade.percentualDesconto(hoje.minusYears(10), hoje));
        });
        teste("Maria (VIP, 2 anos) paga R$ 252,00 no individual", () -> {
            Cenario c = new Cenario();
            Turma t = c.escola.criarTurma(c.piano, TipoAula.INDIVIDUAL, c.ana, c.aula1, seg(14, 15));
            c.escola.matricular(c.maria, t);
            Mensalidade m = c.escola.gerarMensalidade(c.maria, t, HOJE);
            igual(Dinheiro.de("252.00"), m.getValor());
            igual(10, m.getDescontoPercentual());
        });
        teste("não gera mensalidade duplicada pendente, mas gera de novo depois de paga", () -> {
            Cenario c = new Cenario();
            Turma t = c.escola.criarTurma(c.piano, TipoAula.INDIVIDUAL, c.ana, c.aula1, seg(14, 15));
            c.escola.matricular(c.maria, t);
            Mensalidade m = c.escola.gerarMensalidade(c.maria, t, HOJE);
            bloqueia("Já existe uma mensalidade pendente", () -> c.escola.gerarMensalidade(c.maria, t, HOJE));
            c.escola.registrarPagamento(m, HOJE);
            bloqueia("já foi quitado", () -> c.escola.registrarPagamento(m, HOJE));
            c.escola.gerarMensalidade(c.maria, t, HOJE.plusMonths(1));
        });
        teste("só gera mensalidade para aluno matriculado", () -> {
            Cenario c = new Cenario();
            Turma t = c.escola.criarTurma(c.piano, TipoAula.INDIVIDUAL, c.ana, c.aula1, seg(14, 15));
            bloqueia("não está matriculado", () -> c.escola.gerarMensalidade(c.joao, t, HOJE));
        });

        System.out.println("== Aluguel ==");
        LocalDateTime ini = LocalDateTime.of(2026, 11, 24, 18, 0);
        teste("prática custa R$ 30/h para não VIP", () -> {
            Cenario c = new Cenario();
            igual(Dinheiro.de("30.00"), c.escola.alugarSala(c.joao, c.pratica, ini, ini.plusHours(1)).getValor());
        });
        teste("VIP tem 20% de desconto: estúdio 2h = R$ 144,00", () -> {
            Cenario c = new Cenario();
            igual(Dinheiro.de("144.00"), c.escola.alugarSala(c.maria, c.estudio, ini, ini.plusHours(2)).getValor());
        });
        teste("estúdio é exclusivo para VIP", () -> {
            Cenario c = new Cenario();
            bloqueia("exclusivos para alunos VIP", () -> c.escola.alugarSala(c.joao, c.estudio, ini, ini.plusHours(1)));
        });
        teste("sala não pode ser alugada em horário ocupado", () -> {
            Cenario c = new Cenario();
            c.escola.alugarSala(c.joao, c.pratica, ini, ini.plusHours(1));
            bloqueia("já está alugada", () -> c.escola.alugarSala(c.carlos, c.pratica, ini.plusMinutes(30), ini.plusHours(2)));
            c.escola.alugarSala(c.carlos, c.pratica, ini.plusHours(1), ini.plusHours(2)); // logo depois pode
        });
        teste("sala de aula não é alugável", () -> {
            Cenario c = new Cenario();
            bloqueia("não está disponível para aluguel", () -> c.escola.alugarSala(c.maria, c.aula1, ini, ini.plusHours(1)));
        });
        teste("aluguel gera cobrança de taxa pendente", () -> {
            Cenario c = new Cenario();
            c.escola.alugarSala(c.joao, c.pratica, ini, ini.plusHours(1));
            igual(1, c.escola.cobrancasPendentes(c.joao).size());
            igual(Cobranca.Tipo.ALUGUEL, c.escola.cobrancasPendentes(c.joao).get(0).getTipo());
        });
        teste("fim antes do início é recusado", () -> {
            Cenario c = new Cenario();
            bloqueia("fim depois do início", () -> c.escola.alugarSala(c.joao, c.pratica, ini, ini.minusHours(1)));
        });

        System.out.println("== Desempenho ==");
        teste("nota fora de 0 a 10 é recusada; limites 0 e 10 passam", () -> {
            Cenario c = new Cenario();
            bloqueia("entre 0 e 10", () -> c.escola.registrarDesempenho(c.maria, c.piano, 11, "", HOJE));
            bloqueia("entre 0 e 10", () -> c.escola.registrarDesempenho(c.maria, c.piano, -0.1, "", HOJE));
            c.escola.registrarDesempenho(c.maria, c.piano, 0, "", HOJE);
            c.escola.registrarDesempenho(c.maria, c.piano, 10, "", HOJE);
            igual(2, c.maria.getDesempenhos().size());
        });
        teste("relatório de progresso mostra média e última nota", () -> {
            Cenario c = new Cenario();
            c.escola.registrarDesempenho(c.maria, c.piano, 8.0, "a", HOJE.minusDays(30));
            c.escola.registrarDesempenho(c.maria, c.piano, 9.0, "b", HOJE);
            String r = c.escola.relatorioProgresso(c.maria);
            verdade(r.contains("Piano: 2 registro(s)"), r);
            verdade(r.contains("8,50") || r.contains("8.50"), r);
        });
        teste("lista de desempenhos só sai como leitura", () -> {
            Cenario c = new Cenario();
            try {
                c.maria.getDesempenhos().add(new Desempenho(c.piano, 5, "", HOJE));
                throw new AssertionError("a lista deveria ser somente leitura");
            } catch (UnsupportedOperationException esperado) { /* ok */ }
        });

        System.out.println("== Recitais ==");
        LocalDateTime recital = LocalDateTime.of(2026, 12, 20, 19, 0);
        teste("4 vagas: 2 reservadas para VIP; o 3º não VIP é bloqueado antes do prazo", () -> {
            Cenario c = new Cenario();
            Evento e = c.escola.agendarEvento("Recital", recital, 4);
            igual(2, e.getVagasReservadasVip());
            Aluno dora = c.escola.cadastrarAluno("Dora", false, HOJE);
            c.escola.inscreverEmEvento(c.joao, e, HOJE);
            c.escola.inscreverEmEvento(c.carlos, e, HOJE);
            bloqueia("reservadas para alunos VIP", () -> c.escola.inscreverEmEvento(dora, e, HOJE));
            c.escola.inscreverEmEvento(c.maria, e, HOJE); // VIP entra na vaga reservada
        });
        teste("a menos de 7 dias do evento as vagas ficam livres para todos", () -> {
            Cenario c = new Cenario();
            Evento e = c.escola.agendarEvento("Recital", recital, 4);
            LocalDate seteDiasAntes = recital.toLocalDate().minusDays(7);
            c.escola.inscreverEmEvento(c.joao, e, HOJE);
            c.escola.inscreverEmEvento(c.carlos, e, HOJE);
            Aluno dora = c.escola.cadastrarAluno("Dora", false, HOJE);
            bloqueia("reservadas", () -> c.escola.inscreverEmEvento(dora, e, seteDiasAntes.minusDays(1)));
            c.escola.inscreverEmEvento(dora, e, seteDiasAntes.plusDays(1));
        });
        teste("evento lotado, inscrição repetida e evento já realizado", () -> {
            Cenario c = new Cenario();
            Evento e = c.escola.agendarEvento("Recital", recital, 2);
            LocalDate tarde = recital.toLocalDate().minusDays(1);
            c.escola.inscreverEmEvento(c.joao, e, tarde);
            bloqueia("já está inscrito", () -> c.escola.inscreverEmEvento(c.joao, e, tarde));
            c.escola.inscreverEmEvento(c.maria, e, tarde);
            bloqueia("lotado", () -> c.escola.inscreverEmEvento(c.carlos, e, tarde));
            bloqueia("já aconteceu", () -> c.escola.inscreverEmEvento(c.carlos, e, recital.toLocalDate().plusDays(1)));
        });
        teste("5 vagas reservam 2 para VIP (arredonda para baixo)", () -> {
            Cenario c = new Cenario();
            igual(2, c.escola.agendarEvento("R", recital, 5).getVagasReservadasVip());
        });

        System.out.println("== Edição e remoção ==");
        teste("editar aluno muda nome e VIP", () -> {
            Cenario c = new Cenario();
            c.escola.editarAluno(c.joao, "João Pedro", true);
            igual("João Pedro", c.joao.getNome());
            verdade(c.joao.isVip(), "deveria ser VIP");
            bloqueia("em branco", () -> c.escola.editarAluno(c.joao, "  ", true));
            igual("João Pedro", c.joao.getNome());
        });
        teste("não remove professor com turma, nem sala em uso, nem curso em uso", () -> {
            Cenario c = new Cenario();
            Turma t = c.escola.criarTurma(c.piano, TipoAula.INDIVIDUAL, c.ana, c.aula1, seg(14, 15));
            bloqueia("ainda tem turmas", () -> c.escola.removerProfessor(c.ana));
            bloqueia("usada por uma turma", () -> c.escola.removerSala(c.aula1));
            bloqueia("professor habilitado", () -> c.escola.removerCurso(c.piano));
            c.escola.removerTurma(t);
            c.escola.removerProfessor(c.ana);
            c.escola.removerSala(c.aula1);
        });
        teste("não remove turma com alunos; desmatricular libera", () -> {
            Cenario c = new Cenario();
            Turma t = c.escola.criarTurma(c.piano, TipoAula.INDIVIDUAL, c.ana, c.aula1, seg(14, 15));
            c.escola.matricular(c.maria, t);
            bloqueia("ainda tem alunos", () -> c.escola.removerTurma(t));
            c.escola.desmatricular(c.maria, t);
            c.escola.removerTurma(t);
        });
        teste("aluno com cobrança pendente não é removido; depois de quitar, é, com tudo dele", () -> {
            Cenario c = new Cenario();
            Turma t = c.escola.criarTurma(c.piano, TipoAula.INDIVIDUAL, c.ana, c.aula1, seg(14, 15));
            c.escola.matricular(c.maria, t);
            Evento e = c.escola.agendarEvento("Recital", recital, 4);
            c.escola.inscreverEmEvento(c.maria, e, HOJE);
            Mensalidade m = c.escola.gerarMensalidade(c.maria, t, HOJE);
            bloqueia("cobranças pendentes", () -> c.escola.removerAluno(c.maria));
            c.escola.registrarPagamento(m, HOJE);
            c.escola.removerAluno(c.maria);
            verdade(!c.escola.getAlunos().contains(c.maria), "Maria deveria ter saído");
            verdade(t.getAlunos().isEmpty(), "matrícula deveria ter saído");
            verdade(e.getInscritos().isEmpty(), "inscrição deveria ter saído");
            verdade(c.escola.getCobrancas().isEmpty(), "cobranças dela deveriam ter saído");
        });
        teste("cancelar aluguel pendente remove a taxa; aluguel pago não cancela", () -> {
            Cenario c = new Cenario();
            Aluguel a1 = c.escola.alugarSala(c.joao, c.pratica, ini, ini.plusHours(1));
            c.escola.cancelarAluguel(a1);
            verdade(c.escola.getAlugueis().isEmpty() && c.escola.getCobrancas().isEmpty(), "deveria limpar aluguel e taxa");
            Aluguel a2 = c.escola.alugarSala(c.joao, c.pratica, ini, ini.plusHours(1));
            c.escola.registrarPagamento(c.escola.getCobrancas().get(0), HOJE);
            bloqueia("já foi pago", () -> c.escola.cancelarAluguel(a2));
        });
        teste("cancelar inscrição libera a vaga", () -> {
            Cenario c = new Cenario();
            Evento e = c.escola.agendarEvento("Recital", recital, 2);
            c.escola.inscreverEmEvento(c.maria, e, HOJE);
            c.escola.cancelarInscricao(c.maria, e);
            igual(2, e.getVagasLivres());
            bloqueia("não está inscrito", () -> c.escola.cancelarInscricao(c.maria, e));
        });

        System.out.println("== POO: polimorfismo e abstração ==");
        teste("Pessoa: aluno e professor se descrevem de formas diferentes", () -> {
            Cenario c = new Cenario();
            List<String> papeis = c.escola.getPessoas().stream().map(Pessoa::getPapel).toList();
            verdade(papeis.contains("Professor") && papeis.contains("Aluno VIP") && papeis.contains("Aluno"), papeis.toString());
        });
        teste("Cobranca: mensalidade e taxa de aluguel tratadas pela mesma lista", () -> {
            Cenario c = new Cenario();
            Turma t = c.escola.criarTurma(c.piano, TipoAula.INDIVIDUAL, c.ana, c.aula1, seg(14, 15));
            c.escola.matricular(c.maria, t);
            c.escola.gerarMensalidade(c.maria, t, HOJE);
            c.escola.alugarSala(c.maria, c.estudio, ini, ini.plusHours(2));
            List<Cobranca> todas = c.escola.getCobrancas();
            igual(2, todas.size());
            verdade(todas.get(0).getDescricao().startsWith("Mensalidade"), todas.get(0).getDescricao());
            verdade(todas.get(1).getDescricao().startsWith("Aluguel"), todas.get(1).getDescricao());
        });
        teste("Relatorio: interface única para os três relatórios e para o exportador", () -> {
            Cenario c = new Cenario();
            List<Relatorio> relatorios = List.of(new RelatorioProgresso(c.escola, c.maria),
                    new RelatorioFinanceiro(c.escola), new RelatorioEventos(c.escola));
            for (Relatorio r : relatorios) verdade(r.gerar().contains("==="), r.getTitulo());
            igual("relatorio_de_progresso_maria_vip", relatorios.get(0).nomeArquivo());
        });
        teste("relatório financeiro soma cobrado, pago e pendente", () -> {
            Cenario c = new Cenario();
            Turma t = c.escola.criarTurma(c.piano, TipoAula.INDIVIDUAL, c.ana, c.aula1, seg(14, 15));
            c.escola.matricular(c.maria, t);
            Mensalidade m = c.escola.gerarMensalidade(c.maria, t, HOJE);
            c.escola.registrarPagamento(m, HOJE);
            c.escola.alugarSala(c.maria, c.estudio, ini, ini.plusHours(2));
            String r = new RelatorioFinanceiro(c.escola).gerar();
            verdade(r.contains("396,00"), r); // 252 + 144
            verdade(r.contains("144,00"), r);
        });

        System.out.println("== Persistência em CSV ==");
        teste("salvar e carregar devolve a mesma escola (ids, VIP, vínculos, pagamentos, notas)", () -> {
            Path pasta = pastaTemporaria();
            Cenario c = new Cenario();
            Turma t = c.escola.criarTurma(c.piano, TipoAula.INDIVIDUAL, c.ana, c.aula1, seg(14, 15));
            c.escola.matricular(c.maria, t);
            Mensalidade m = c.escola.gerarMensalidade(c.maria, t, HOJE);
            c.escola.registrarPagamento(m, HOJE);
            c.escola.alugarSala(c.maria, c.estudio, ini, ini.plusHours(2));
            Evento e = c.escola.agendarEvento("Recital", recital, 4);
            c.escola.inscreverEmEvento(c.joao, e, HOJE);
            c.escola.inscreverEmEvento(c.maria, e, HOJE);
            c.escola.registrarDesempenho(c.maria, c.piano, 9.5, "ótimo; com ponto e vírgula\ne quebra de linha", HOJE);

            RepositorioCsv repo = new RepositorioCsv(pasta);
            repo.salvar(c.escola);
            Escola nova = new Escola("Outra");
            List<String> avisos = repo.carregar(nova);
            verdade(avisos.isEmpty(), "avisos inesperados: " + avisos);

            igual(c.escola.getCursos().size(), nova.getCursos().size());
            igual(c.escola.getProfessores().size(), nova.getProfessores().size());
            igual(c.escola.getAlunos().size(), nova.getAlunos().size());
            igual(c.escola.getSalas().size(), nova.getSalas().size());
            igual(c.escola.getTurmas().size(), nova.getTurmas().size());
            igual(c.escola.getAlugueis().size(), nova.getAlugueis().size());
            igual(c.escola.getCobrancas().size(), nova.getCobrancas().size());
            Aluno maria = nova.getAlunos().stream().filter(a -> a.getNome().equals("Maria")).findFirst().orElseThrow();
            verdade(maria.isVip(), "Maria deveria continuar VIP");
            igual(c.maria.getId(), maria.getId());
            igual(c.maria.getDataMatricula(), maria.getDataMatricula());
            igual(1, nova.getTurmas().get(0).getAlunos().size());
            igual(2, nova.getEventos().get(0).getInscritos().size());
            igual("ótimo; com ponto e vírgula\ne quebra de linha", maria.getDesempenhos().get(0).getObservacao());
            Cobranca pago = nova.getCobrancas().stream().filter(Cobranca::isPago).findFirst().orElseThrow();
            igual(Dinheiro.de("252.00"), pago.getValor());
            igual("Mensalidade Piano (Individual), fidelidade -10%", pago.getDescricao());
            // o que foi carregado continua obedecendo às regras
            bloqueia("já está alugada", () -> nova.alugarSala(maria, nova.getSalas().get(3), ini, ini.plusHours(1)));
            // e novos ids não repetem os antigos
            Aluno novo = nova.cadastrarAluno("Novo", false, HOJE);
            verdade(novo.getId() > c.escola.getProximoId() - 1, "id novo repetiu um existente: " + novo.getId());
            apagar(pasta);
        });
        teste("pasta de dados ausente: começa vazio com um aviso, sem erro", () -> {
            Path pasta = pastaTemporaria().resolve("nao-existe");
            Escola e = new Escola("X");
            List<String> avisos = new RepositorioCsv(pasta).carregar(e);
            igual(1, avisos.size());
            verdade(e.estaVazia(), "deveria estar vazia");
        });
        teste("arquivo ausente na pasta: só avisa e carrega o resto", () -> {
            Path pasta = pastaTemporaria();
            Cenario c = new Cenario();
            RepositorioCsv repo = new RepositorioCsv(pasta);
            repo.salvar(c.escola);
            Files.delete(pasta.resolve("salas.csv"));
            Escola e = new Escola("X");
            List<String> avisos = repo.carregar(e);
            verdade(avisos.stream().anyMatch(a -> a.contains("salas.csv")), avisos.toString());
            igual(3, e.getAlunos().size());
            apagar(pasta);
        });
        teste("linha corrompida é ignorada com aviso; as outras carregam", () -> {
            Path pasta = pastaTemporaria();
            Cenario c = new Cenario();
            RepositorioCsv repo = new RepositorioCsv(pasta);
            repo.salvar(c.escola);
            Path alunos = pasta.resolve("alunos.csv");
            Files.writeString(alunos, Files.readString(alunos) + "lixo sem colunas\n99;Fulano;talvez;data-errada\n", StandardCharsets.UTF_8);
            Escola e = new Escola("X");
            List<String> avisos = repo.carregar(e);
            igual(2L, avisos.stream().filter(a -> a.contains("alunos.csv")).count());
            igual(3, e.getAlunos().size());
            apagar(pasta);
        });
        teste("vínculo quebrado (turma de professor inexistente) é ignorado, sem derrubar o sistema", () -> {
            Path pasta = pastaTemporaria();
            Cenario c = new Cenario();
            c.escola.criarTurma(c.piano, TipoAula.INDIVIDUAL, c.ana, c.aula1, seg(14, 15));
            RepositorioCsv repo = new RepositorioCsv(pasta);
            repo.salvar(c.escola);
            Files.writeString(pasta.resolve("professores.csv"), "id;nome\n", StandardCharsets.UTF_8); // apaga os professores
            Escola e = new Escola("X");
            List<String> avisos = repo.carregar(e);
            verdade(avisos.stream().anyMatch(a -> a.contains("turmas.csv")), avisos.toString());
            igual(0, e.getTurmas().size());
            apagar(pasta);
        });

        System.out.println();
        System.out.println("Resultado: " + passaram + " passaram, " + falharam + " falharam.");
        if (falharam > 0) System.exit(1);
    }

    private static Path pastaTemporaria() {
        try {
            return Files.createTempDirectory("notamaxima-teste");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void apagar(Path pasta) {
        try (Stream<Path> arquivos = Files.walk(pasta)) {
            arquivos.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
        } catch (IOException ignorado) { /* limpeza best-effort */ }
    }
}
