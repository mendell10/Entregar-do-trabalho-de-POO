package escola;

import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

/**
 * Menu interativo no console. Só lê dados e mostra resultados: toda regra de negócio fica na Escola.
 * Os dados são carregados ao abrir e salvos depois de cada ação, então nada se perde.
 */
public class Menu {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final List<String> CURSOS_PADRAO = List.of("Piano", "Violão", "Canto");

    /** Lançada quando a entrada do teclado acaba (Ctrl+D / arquivo redirecionado terminou). */
    private static final class EntradaEncerrada extends RuntimeException {
        private static final long serialVersionUID = 1L;

        EntradaEncerrada() { super("Entrada encerrada"); }
    }

    private final Scanner in = new Scanner(System.in);
    private final Escola escola = new Escola("Nota Máxima");
    private final RepositorioCsv repositorio;
    private LocalDate hoje = LocalDate.now();

    public Menu(RepositorioCsv repositorio) {
        this.repositorio = repositorio;
    }

    public void executar() {
        carregarDados();
        try {
            boolean rodando = true;
            while (rodando) {
                mostrarMenu();
                int op = lerInt("Escolha: ");
                // cada ação roda dentro do tratamento de erros: uma regra violada só mostra a mensagem
                try {
                    switch (op) {
                        case 1 -> menuCadastros();
                        case 2 -> menuTurmas();
                        case 3 -> menuPagamentos();
                        case 4 -> menuAluguel();
                        case 5 -> menuDesempenho();
                        case 6 -> menuEventos();
                        case 7 -> listarTudo();
                        case 8 -> carregarExemplo();
                        case 9 -> hoje = lerData("Nova data de hoje (dd/mm/aaaa): ");
                        case 10 -> menuEditarRemover();
                        case 11 -> menuRelatorios();
                        case 12 -> menuDados();
                        case 0 -> rodando = false;
                        default -> System.out.println("Opção inválida.");
                    }
                } catch (EntradaEncerrada e) {
                    throw e;
                } catch (RegraDeNegocioException e) {
                    System.out.println(">> BLOQUEADO: " + e.getMessage());
                } catch (DateTimeException | IllegalArgumentException e) {
                    System.out.println(">> Entrada inválida: " + e.getMessage());
                } catch (RuntimeException e) {
                    System.out.println(">> Algo deu errado, mas o sistema continua: " + e.getMessage());
                }
                salvarDados();
            }
        } catch (EntradaEncerrada e) {
            System.out.println("\n(entrada encerrada)");
            salvarDados();
        }
        System.out.println("Até logo!");
    }

    private void mostrarMenu() {
        System.out.println("\n===== ESCOLA NOTA MÁXIMA  |  hoje: " + hoje.format(DATA) + " =====");
        System.out.println(" 1. Cadastros (curso, professor, aluno, sala)");
        System.out.println(" 2. Turmas e matrículas");
        System.out.println(" 3. Mensalidades e pagamentos");
        System.out.println(" 4. Aluguel de salas e estúdios");
        System.out.println(" 5. Desempenho e relatório de progresso");
        System.out.println(" 6. Recitais e eventos");
        System.out.println(" 7. Listar tudo");
        System.out.println(" 8. Carregar dados de exemplo");
        System.out.println(" 9. Alterar a data de hoje (para simular prazos e fidelidade)");
        System.out.println("10. Editar / remover");
        System.out.println("11. Relatórios (ver na tela ou salvar em arquivo)");
        System.out.println("12. Dados (salvar agora, zerar tudo)");
        System.out.println(" 0. Sair");
    }

    // ---------- persistência ----------

    private void carregarDados() {
        try {
            List<String> avisos = repositorio.carregar(escola);
            if (avisos.isEmpty()) {
                System.out.println("Dados carregados de '" + repositorio.getPasta() + "'.");
            } else {
                avisos.forEach(a -> System.out.println("[aviso] " + a));
            }
        } catch (RuntimeException e) {
            System.out.println("[aviso] Não foi possível carregar os dados (" + e.getMessage() + "). Começando vazio.");
            escola.limparTudo();
        }
        garantirCursosPadrao();
    }

    private void garantirCursosPadrao() {
        for (String nome : CURSOS_PADRAO) {
            if (escola.getCursos().stream().noneMatch(c -> c.getNome().equalsIgnoreCase(nome))) {
                escola.cadastrarCurso(nome);
            }
        }
    }

    private void salvarDados() {
        try {
            repositorio.salvar(escola);
        } catch (PersistenciaException e) {
            System.out.println("[aviso] " + e.getMessage());
        }
    }

    // ---------- submenus ----------

    private void menuCadastros() {
        System.out.println("1. Curso  2. Professor  3. Aluno  4. Sala");
        switch (lerInt("Escolha: ")) {
            case 1 -> System.out.println("Cadastrado: " + escola.cadastrarCurso(lerTexto("Nome do curso: ")));
            case 2 -> {
                String nome = lerTexto("Nome do professor: ");
                Set<Curso> cursos = new LinkedHashSet<>();
                System.out.println("Cursos que ele leciona (um por vez; 0 para terminar):");
                while (true) {
                    Curso c = escolher(escola.getCursos(), "Curso (0 = fim): ");
                    if (c == null) break;
                    cursos.add(c);
                }
                System.out.println("Cadastrado: " + escola.cadastrarProfessor(nome, cursos));
            }
            case 3 -> {
                String nome = lerTexto("Nome do aluno: ");
                boolean vip = lerSimNao("Aluno VIP? (s/n): ");
                LocalDate matricula = lerData("Data de matrícula na escola (dd/mm/aaaa): ");
                System.out.println("Cadastrado: " + escola.cadastrarAluno(nome, vip, matricula));
            }
            case 4 -> {
                String nome = lerTexto("Nome da sala: ");
                TipoSala tipo = escolher(List.of(TipoSala.values()), "Tipo (0 = cancelar): ");
                if (tipo != null) System.out.println("Cadastrada: " + escola.cadastrarSala(nome, tipo));
            }
            default -> System.out.println("Opção inválida.");
        }
    }

    private void menuTurmas() {
        System.out.println("1. Criar turma  2. Matricular aluno  3. Listar turmas");
        switch (lerInt("Escolha: ")) {
            case 1 -> {
                Curso curso = escolher(escola.getCursos(), "Curso: ");
                if (curso == null) return;
                TipoAula tipo = escolher(List.of(TipoAula.values()), "Tipo de aula: ");
                if (tipo == null) return;
                Professor prof = escolher(escola.getProfessores(), "Professor: ");
                if (prof == null) return;
                Sala sala = escolher(escola.getSalas(), "Sala: ");
                if (sala == null) return;
                Horario h = lerHorario();
                System.out.println("Criada: " + escola.criarTurma(curso, tipo, prof, sala, h));
            }
            case 2 -> {
                Aluno aluno = escolher(escola.getAlunos(), "Aluno: ");
                if (aluno == null) return;
                Turma turma = escolher(escola.getTurmas(), "Turma: ");
                if (turma == null) return;
                escola.matricular(aluno, turma);
                System.out.println(aluno.getNome() + " matriculado. Vagas restantes: " + turma.getVagasDisponiveis());
            }
            case 3 -> listar("Turmas", escola.getTurmas());
            default -> System.out.println("Opção inválida.");
        }
    }

    private void menuPagamentos() {
        System.out.println("1. Gerar mensalidade  2. Pagar  3. Pendentes de um aluno  4. Listar todos");
        switch (lerInt("Escolha: ")) {
            case 1 -> {
                Aluno aluno = escolher(escola.getAlunos(), "Aluno: ");
                if (aluno == null) return;
                Turma turma = escolher(escola.getTurmas(), "Turma: ");
                if (turma == null) return;
                int desc = PlanoFidelidade.percentualDesconto(aluno.getDataMatricula(), hoje);
                System.out.println("Desconto de fidelidade do aluno: " + desc + "%");
                System.out.println("Gerado: " + escola.gerarMensalidade(aluno, turma, hoje));
            }
            case 2 -> {
                Cobranca c = escolher(escola.getCobrancas().stream().filter(x -> !x.isPago()).toList(),
                        "Cobrança pendente: ");
                if (c == null) return;
                escola.registrarPagamento(c, hoje);
                System.out.println("Quitado: " + c);
            }
            case 3 -> {
                Aluno aluno = escolher(escola.getAlunos(), "Aluno: ");
                if (aluno != null) listar("Pendentes", escola.cobrancasPendentes(aluno));
            }
            case 4 -> listar("Cobranças", escola.getCobrancas());
            default -> System.out.println("Opção inválida.");
        }
    }

    private void menuAluguel() {
        System.out.println("1. Alugar sala/estúdio  2. Listar aluguéis");
        switch (lerInt("Escolha: ")) {
            case 1 -> {
                Aluno aluno = escolher(escola.getAlunos(), "Aluno: ");
                if (aluno == null) return;
                Sala sala = escolher(escola.getSalas(), "Sala: ");
                if (sala == null) return;
                LocalDateTime ini = lerDataHora("Início (dd/mm/aaaa hh:mm): ");
                LocalDateTime fim = lerDataHora("Fim (dd/mm/aaaa hh:mm): ");
                System.out.println("Alugado: " + escola.alugarSala(aluno, sala, ini, fim));
            }
            case 2 -> listar("Aluguéis", escola.getAlugueis());
            default -> System.out.println("Opção inválida.");
        }
    }

    private void menuDesempenho() {
        System.out.println("1. Registrar nota  2. Relatório de progresso");
        switch (lerInt("Escolha: ")) {
            case 1 -> {
                Aluno aluno = escolher(escola.getAlunos(), "Aluno: ");
                if (aluno == null) return;
                Curso curso = escolher(escola.getCursos(), "Curso: ");
                if (curso == null) return;
                double nota = lerDouble("Nota (0 a 10): ");
                String obs = lerTexto("Observação: ");
                escola.registrarDesempenho(aluno, curso, nota, obs, hoje);
                System.out.println("Registrado.");
            }
            case 2 -> {
                Aluno aluno = escolher(escola.getAlunos(), "Aluno: ");
                if (aluno != null) System.out.print(escola.relatorioProgresso(aluno));
            }
            default -> System.out.println("Opção inválida.");
        }
    }

    private void menuEventos() {
        System.out.println("1. Agendar evento  2. Inscrever aluno  3. Listar eventos");
        switch (lerInt("Escolha: ")) {
            case 1 -> {
                String nome = lerTexto("Nome do evento: ");
                LocalDateTime dh = lerDataHora("Data e hora (dd/mm/aaaa hh:mm): ");
                int vagas = lerInt("Número de vagas: ");
                Evento e = escola.agendarEvento(nome, dh, vagas);
                System.out.println("Agendado: " + e + " | reservadas para VIP: " + e.getVagasReservadasVip());
            }
            case 2 -> {
                Aluno aluno = escolher(escola.getAlunos(), "Aluno: ");
                if (aluno == null) return;
                Evento ev = escolher(escola.getEventos(), "Evento: ");
                if (ev == null) return;
                escola.inscreverEmEvento(aluno, ev, hoje);
                System.out.println("Inscrição feita! " + ev);
            }
            case 3 -> {
                for (Evento e : escola.getEventos()) {
                    System.out.println(e + " | inscritos: " + e.getInscritos());
                }
                if (escola.getEventos().isEmpty()) System.out.println("(nenhum evento)");
            }
            default -> System.out.println("Opção inválida.");
        }
    }

    private void menuEditarRemover() {
        System.out.println("Editar:   1. Aluno (nome/VIP)  2. Professor  3. Sala  4. Evento");
        System.out.println("Remover:  5. Aluno  6. Professor  7. Curso  8. Sala  9. Turma  10. Evento");
        System.out.println("Cancelar: 11. Matrícula em turma  12. Aluguel  13. Inscrição em evento");
        switch (lerInt("Escolha: ")) {
            case 1 -> {
                Aluno a = escolher(escola.getAlunos(), "Aluno: ");
                if (a == null) return;
                String nome = lerTexto("Novo nome (Enter mantém): ");
                String vip = lerTexto("VIP? (s/n, Enter mantém): ");
                escola.editarAluno(a, nome.isBlank() ? a.getNome() : nome,
                        vip.isBlank() ? a.isVip() : vip.toLowerCase().startsWith("s"));
                System.out.println("Atualizado: " + a);
            }
            case 2 -> {
                Professor p = escolher(escola.getProfessores(), "Professor: ");
                if (p == null) return;
                escola.editarProfessor(p, lerTexto("Novo nome: "));
                System.out.println("Atualizado: " + p);
            }
            case 3 -> {
                Sala s = escolher(escola.getSalas(), "Sala: ");
                if (s == null) return;
                escola.editarSala(s, lerTexto("Novo nome: "));
                System.out.println("Atualizada: " + s);
            }
            case 4 -> {
                Evento e = escolher(escola.getEventos(), "Evento: ");
                if (e == null) return;
                escola.editarEvento(e, lerTexto("Novo nome: "));
                System.out.println("Atualizado: " + e);
            }
            case 5 -> {
                Aluno a = escolher(escola.getAlunos(), "Aluno a remover: ");
                if (a == null) return;
                escola.removerAluno(a);
                System.out.println("Removido: " + a);
            }
            case 6 -> {
                Professor p = escolher(escola.getProfessores(), "Professor a remover: ");
                if (p == null) return;
                escola.removerProfessor(p);
                System.out.println("Removido: " + p);
            }
            case 7 -> {
                Curso c = escolher(escola.getCursos(), "Curso a remover: ");
                if (c == null) return;
                escola.removerCurso(c);
                System.out.println("Removido: " + c);
            }
            case 8 -> {
                Sala s = escolher(escola.getSalas(), "Sala a remover: ");
                if (s == null) return;
                escola.removerSala(s);
                System.out.println("Removida: " + s);
            }
            case 9 -> {
                Turma t = escolher(escola.getTurmas(), "Turma a remover: ");
                if (t == null) return;
                escola.removerTurma(t);
                System.out.println("Removida.");
            }
            case 10 -> {
                Evento e = escolher(escola.getEventos(), "Evento a remover: ");
                if (e == null) return;
                escola.removerEvento(e);
                System.out.println("Removido: " + e);
            }
            case 11 -> {
                Turma t = escolher(escola.getTurmas(), "Turma: ");
                if (t == null) return;
                Aluno a = escolher(t.getAlunos(), "Aluno a desmatricular: ");
                if (a == null) return;
                escola.desmatricular(a, t);
                System.out.println(a.getNome() + " desmatriculado.");
            }
            case 12 -> {
                Aluguel a = escolher(escola.getAlugueis(), "Aluguel a cancelar: ");
                if (a == null) return;
                escola.cancelarAluguel(a);
                System.out.println("Aluguel cancelado.");
            }
            case 13 -> {
                Evento e = escolher(escola.getEventos(), "Evento: ");
                if (e == null) return;
                Aluno a = escolher(e.getInscritos(), "Aluno a desinscrever: ");
                if (a == null) return;
                escola.cancelarInscricao(a, e);
                System.out.println("Inscrição cancelada.");
            }
            default -> System.out.println("Opção inválida.");
        }
    }

    private void menuRelatorios() {
        // todos os relatórios são tratados pela interface Relatorio (polimorfismo)
        System.out.println("1. Progresso de um aluno  2. Financeiro  3. Recitais e eventos");
        Relatorio relatorio = null;
        switch (lerInt("Escolha: ")) {
            case 1 -> {
                Aluno aluno = escolher(escola.getAlunos(), "Aluno: ");
                if (aluno != null) relatorio = new RelatorioProgresso(escola, aluno);
            }
            case 2 -> relatorio = new RelatorioFinanceiro(escola);
            case 3 -> relatorio = new RelatorioEventos(escola);
            default -> System.out.println("Opção inválida.");
        }
        if (relatorio == null) return;
        System.out.print(relatorio.gerar());
        if (lerSimNao("Salvar em arquivo? (s/n): ")) {
            Path arquivo = ExportadorRelatorio.salvar(relatorio, Path.of("relatorios"));
            System.out.println("Relatório salvo em: " + arquivo.toAbsolutePath());
        }
    }

    private void menuDados() {
        System.out.println("1. Salvar agora  2. Zerar todos os dados  3. Mostrar pasta de dados");
        switch (lerInt("Escolha: ")) {
            case 1 -> {
                repositorio.salvar(escola);
                System.out.println("Salvo em " + repositorio.getPasta().toAbsolutePath());
            }
            case 2 -> {
                if (lerSimNao("Apagar TODOS os dados da escola? (s/n): ")) {
                    escola.limparTudo();
                    garantirCursosPadrao();
                    System.out.println("Escola zerada (os 3 cursos padrão foram recriados).");
                } else {
                    System.out.println("Nada foi apagado.");
                }
            }
            case 3 -> System.out.println("Pasta de dados: " + repositorio.getPasta().toAbsolutePath());
            default -> System.out.println("Opção inválida.");
        }
    }

    private void listarTudo() {
        listar("Cursos", escola.getCursos());
        // getPessoas() devolve Pessoa; cada uma se descreve do seu jeito (polimorfismo)
        System.out.println("--- Pessoas ---");
        if (escola.getPessoas().isEmpty()) System.out.println("(vazio)");
        for (Pessoa p : escola.getPessoas()) System.out.println("  [" + p.getPapel() + "] " + p.descricaoDetalhada());
        listar("Salas", escola.getSalas());
        listar("Turmas", escola.getTurmas());
        listar("Aluguéis", escola.getAlugueis());
        listar("Cobranças", escola.getCobrancas());
        listar("Eventos", escola.getEventos());
    }

    private void carregarExemplo() {
        if (!escola.estaVazia()) {
            throw new RegraDeNegocioException("A escola já tem dados. Use 12 > Zerar todos os dados antes de carregar o exemplo.");
        }
        Curso piano = escola.getCursos().stream().filter(c -> c.getNome().equals("Piano")).findFirst().orElseThrow();
        Curso violao = escola.getCursos().stream().filter(c -> c.getNome().equals("Violão")).findFirst().orElseThrow();
        Curso canto = escola.getCursos().stream().filter(c -> c.getNome().equals("Canto")).findFirst().orElseThrow();
        Professor ana = escola.cadastrarProfessor("Ana", Set.of(piano, canto));
        Professor bruno = escola.cadastrarProfessor("Bruno", Set.of(violao));
        Sala aula1 = escola.cadastrarSala("Sala 1", TipoSala.AULA);
        escola.cadastrarSala("Prática A", TipoSala.PRATICA);
        escola.cadastrarSala("Estúdio", TipoSala.ESTUDIO);
        Aluno maria = escola.cadastrarAluno("Maria", true, hoje.minusYears(2).minusDays(10));
        Aluno joao = escola.cadastrarAluno("João", false, hoje.minusMonths(3));
        escola.cadastrarAluno("Carlos", false, hoje);
        Turma t1 = escola.criarTurma(piano, TipoAula.INDIVIDUAL, ana, aula1,
                new Horario(DayOfWeek.MONDAY, LocalTime.of(14, 0), LocalTime.of(15, 0)));
        Turma t2 = escola.criarTurma(violao, TipoAula.EM_GRUPO, bruno, aula1,
                new Horario(DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(11, 0)));
        escola.matricular(maria, t1);
        escola.matricular(joao, t2);
        System.out.println("Dados de exemplo carregados (professores, salas, alunos Maria VIP/João/Carlos, 2 turmas).");
    }

    // ---------- leitura de dados ----------

    private <T> T escolher(List<T> itens, String pergunta) {
        if (itens.isEmpty()) {
            System.out.println("(nada cadastrado ainda para esta escolha)");
            return null;
        }
        for (int i = 0; i < itens.size(); i++) System.out.println("  " + (i + 1) + ". " + itens.get(i));
        int n = lerInt(pergunta);
        if (n < 1 || n > itens.size()) return null;
        return itens.get(n - 1);
    }

    private <T> void listar(String titulo, List<T> itens) {
        System.out.println("--- " + titulo + " ---");
        if (itens.isEmpty()) System.out.println("(vazio)");
        itens.forEach(i -> System.out.println("  " + i));
    }

    private Horario lerHorario() {
        System.out.println("Dia: 1 seg, 2 ter, 3 qua, 4 qui, 5 sex, 6 sáb, 7 dom");
        DayOfWeek dia = DayOfWeek.of(lerInt("Dia: "));
        LocalTime ini = LocalTime.parse(lerTexto("Início (hh:mm): "));
        LocalTime fim = LocalTime.parse(lerTexto("Fim (hh:mm): "));
        return new Horario(dia, ini, fim);
    }

    private String lerTexto(String msg) {
        System.out.print(msg);
        try {
            return in.nextLine().trim();
        } catch (NoSuchElementException e) {
            throw new EntradaEncerrada();
        }
    }

    private int lerInt(String msg) {
        while (true) {
            try {
                return Integer.parseInt(lerTexto(msg));
            } catch (NumberFormatException e) {
                System.out.println("Digite um número inteiro.");
            }
        }
    }

    private double lerDouble(String msg) {
        while (true) {
            try {
                return Double.parseDouble(lerTexto(msg).replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Digite um número (ex.: 8,5).");
            }
        }
    }

    private boolean lerSimNao(String msg) {
        return lerTexto(msg).toLowerCase().startsWith("s");
    }

    private LocalDate lerData(String msg) {
        return LocalDate.parse(lerTexto(msg), DATA);
    }

    private LocalDateTime lerDataHora(String msg) {
        return LocalDateTime.parse(lerTexto(msg), DATA_HORA);
    }
}
