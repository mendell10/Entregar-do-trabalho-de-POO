package escola;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Persistência da escola em arquivos CSV (um por tipo de dado), dentro de uma pasta.
 * Ao carregar, nada quebra: pasta ou arquivo ausente vira escola vazia, e linha inválida
 * é ignorada com um aviso para o usuário.
 */
public final class RepositorioCsv {
    private final Path pasta;

    public RepositorioCsv(Path pasta) { this.pasta = pasta; }

    public Path getPasta() { return pasta; }

    // ------------------------------------------------------------------ salvar

    public void salvar(Escola e) {
        try {
            Files.createDirectories(pasta);
            escrever("cursos.csv", "nome", e.getCursos().stream().map(c -> Csv.linha(c.getNome())).toList());

            List<String> professores = new ArrayList<>(), habilitacoes = new ArrayList<>();
            for (Professor p : e.getProfessores()) {
                professores.add(Csv.linha(p.getId(), p.getNome()));
                for (Curso c : p.getCursosHabilitados()) habilitacoes.add(Csv.linha(p.getId(), c.getNome()));
            }
            escrever("professores.csv", "id;nome", professores);
            escrever("habilitacoes.csv", "professorId;curso", habilitacoes);

            escrever("alunos.csv", "id;nome;vip;dataMatricula", e.getAlunos().stream()
                    .map(a -> Csv.linha(a.getId(), a.getNome(), a.isVip(), a.getDataMatricula())).toList());
            escrever("salas.csv", "id;nome;tipo", e.getSalas().stream()
                    .map(s -> Csv.linha(s.getId(), s.getNome(), s.getTipo().name())).toList());

            List<String> turmas = new ArrayList<>(), matriculas = new ArrayList<>();
            for (Turma t : e.getTurmas()) {
                Horario h = t.getHorario();
                turmas.add(Csv.linha(t.getId(), t.getCurso().getNome(), t.getTipo().name(), t.getProfessor().getId(),
                        t.getSala().getId(), h.getDia().name(), h.getInicio(), h.getFim()));
                for (Aluno a : t.getAlunos()) matriculas.add(Csv.linha(t.getId(), a.getId()));
            }
            escrever("turmas.csv", "id;curso;tipoAula;professorId;salaId;dia;inicio;fim", turmas);
            escrever("matriculas.csv", "turmaId;alunoId", matriculas);

            escrever("alugueis.csv", "id;alunoId;salaId;inicio;fim;valor", e.getAlugueis().stream()
                    .map(a -> Csv.linha(a.getId(), a.getAluno().getId(), a.getSala().getId(), a.getInicio(), a.getFim(),
                            a.getValor().toPlainString())).toList());

            List<String> eventos = new ArrayList<>(), inscricoes = new ArrayList<>();
            for (Evento ev : e.getEventos()) {
                eventos.add(Csv.linha(ev.getId(), ev.getNome(), ev.getDataHora(), ev.getVagasTotais()));
                for (Aluno a : ev.getInscritos()) inscricoes.add(Csv.linha(ev.getId(), a.getId()));
            }
            escrever("eventos.csv", "id;nome;dataHora;vagas", eventos);
            escrever("inscricoes.csv", "eventoId;alunoId", inscricoes);

            escrever("cobrancas.csv", "id;tipo;alunoId;valor;dataPagamento;referenciaId;detalhe;descontoPercentual",
                    e.getCobrancas().stream().map(c -> Csv.linha(c.getId(), c.getTipo().name(), c.getAluno().getId(),
                            c.getValor().toPlainString(), c.getDataPagamento(), c.getReferenciaId(), c.getDetalhe(),
                            c.getDescontoPercentual())).toList());

            List<String> desempenhos = new ArrayList<>();
            for (Aluno a : e.getAlunos()) {
                for (Desempenho d : a.getDesempenhos()) {
                    desempenhos.add(Csv.linha(a.getId(), d.getCurso().getNome(), d.getNota(), d.getObservacao(), d.getData()));
                }
            }
            escrever("desempenhos.csv", "alunoId;curso;nota;observacao;data", desempenhos);
            escrever("meta.csv", "chave;valor", List.of(Csv.linha("proximoId", e.getProximoId())));
        } catch (IOException ex) {
            throw new PersistenciaException("Não foi possível salvar em '" + pasta + "': " + ex.getMessage(), ex);
        }
    }

    /** Grava em arquivo temporário e troca no final, para não deixar arquivo pela metade. */
    private void escrever(String arquivo, String cabecalho, List<String> linhas) throws IOException {
        List<String> todas = new ArrayList<>();
        todas.add(cabecalho);
        todas.addAll(linhas);
        Path destino = pasta.resolve(arquivo);
        Path temp = pasta.resolve(arquivo + ".tmp");
        Files.write(temp, todas, StandardCharsets.UTF_8);
        try {
            Files.move(temp, destino, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(temp, destino, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // ----------------------------------------------------------------- carregar

    /**
     * Substitui o conteúdo da escola pelo que está nos arquivos.
     * @return avisos para mostrar ao usuário (lista vazia = tudo certo)
     */
    public List<String> carregar(Escola e) {
        List<String> avisos = new ArrayList<>();
        e.limparTudo();
        if (!Files.isDirectory(pasta)) {
            avisos.add("Nenhum dado salvo em '" + pasta + "'. Começando com a escola vazia.");
            return avisos;
        }
        Map<Integer, Professor> professores = new HashMap<>();
        Map<Integer, Aluno> alunos = new HashMap<>();
        Map<Integer, Sala> salas = new HashMap<>();
        Map<Integer, Turma> turmas = new HashMap<>();
        Map<Integer, Evento> eventos = new HashMap<>();
        Map<Integer, Set<Curso>> habilitacoes = new HashMap<>();
        int[] meta = {1};

        ler("cursos.csv", avisos, c -> e.restaurar(new Curso(c.get(0))));
        ler("habilitacoes.csv", avisos, c ->
                habilitacoes.computeIfAbsent(Integer.parseInt(c.get(0)), k -> new LinkedHashSet<>()).add(curso(e, c.get(1))));
        ler("professores.csv", avisos, c -> {
            int id = Integer.parseInt(c.get(0));
            Professor p = new Professor(id, c.get(1), habilitacoes.get(id));
            e.restaurar(p);
            professores.put(id, p);
        });
        ler("alunos.csv", avisos, c -> {
            Aluno a = new Aluno(Integer.parseInt(c.get(0)), c.get(1), Boolean.parseBoolean(c.get(2)), LocalDate.parse(c.get(3)));
            e.restaurar(a);
            alunos.put(a.getId(), a);
        });
        ler("salas.csv", avisos, c -> {
            Sala s = new Sala(Integer.parseInt(c.get(0)), c.get(1), TipoSala.valueOf(c.get(2)));
            e.restaurar(s);
            salas.put(s.getId(), s);
        });
        ler("turmas.csv", avisos, c -> {
            Horario h = new Horario(DayOfWeek.valueOf(c.get(5)), LocalTime.parse(c.get(6)), LocalTime.parse(c.get(7)));
            Turma t = new Turma(Integer.parseInt(c.get(0)), curso(e, c.get(1)), TipoAula.valueOf(c.get(2)),
                    buscar(professores, c.get(3), "professor"), buscar(salas, c.get(4), "sala"), h);
            e.restaurar(t);
            turmas.put(t.getId(), t);
        });
        ler("matriculas.csv", avisos, c ->
                buscar(turmas, c.get(0), "turma").adicionarAluno(buscar(alunos, c.get(1), "aluno")));
        ler("alugueis.csv", avisos, c -> e.restaurar(new Aluguel(Integer.parseInt(c.get(0)),
                buscar(alunos, c.get(1), "aluno"), buscar(salas, c.get(2), "sala"),
                LocalDateTime.parse(c.get(3)), LocalDateTime.parse(c.get(4)), new BigDecimal(c.get(5)))));
        ler("eventos.csv", avisos, c -> {
            Evento ev = new Evento(Integer.parseInt(c.get(0)), c.get(1), LocalDateTime.parse(c.get(2)), Integer.parseInt(c.get(3)));
            e.restaurar(ev);
            eventos.put(ev.getId(), ev);
        });
        ler("inscricoes.csv", avisos, c ->
                buscar(eventos, c.get(0), "evento").restaurarInscrito(buscar(alunos, c.get(1), "aluno")));
        ler("cobrancas.csv", avisos, c -> {
            int id = Integer.parseInt(c.get(0));
            Aluno aluno = buscar(alunos, c.get(2), "aluno");
            BigDecimal valor = new BigDecimal(c.get(3));
            int ref = Integer.parseInt(c.get(5));
            Cobranca cobranca = switch (Cobranca.Tipo.valueOf(c.get(1))) {
                case MENSALIDADE -> new Mensalidade(id, aluno, ref, c.get(6), Integer.parseInt(c.get(7)), valor);
                case ALUGUEL -> new TaxaAluguel(id, aluno, ref, c.get(6), valor);
            };
            if (!c.get(4).isBlank()) cobranca.pagar(LocalDate.parse(c.get(4)));
            e.restaurar(cobranca);
        });
        ler("desempenhos.csv", avisos, c -> buscar(alunos, c.get(0), "aluno").adicionarDesempenho(
                new Desempenho(curso(e, c.get(1)), Double.parseDouble(c.get(2)), c.get(3), LocalDate.parse(c.get(4)))));
        ler("meta.csv", avisos, c -> { if (c.get(0).equals("proximoId")) meta[0] = Integer.parseInt(c.get(1)); });

        // garante que novos ids nunca repitam um id carregado, mesmo se o meta.csv estiver ruim
        int maior = 0;
        for (Pessoa p : e.getPessoas()) maior = Math.max(maior, p.getId());
        for (Sala s : e.getSalas()) maior = Math.max(maior, s.getId());
        for (Turma t : e.getTurmas()) maior = Math.max(maior, t.getId());
        for (Aluguel a : e.getAlugueis()) maior = Math.max(maior, a.getId());
        for (Evento ev : e.getEventos()) maior = Math.max(maior, ev.getId());
        for (Cobranca c : e.getCobrancas()) maior = Math.max(maior, c.getId());
        e.definirProximoId(Math.max(meta[0], maior + 1));
        return avisos;
    }

    private void ler(String arquivo, List<String> avisos, Consumer<List<String>> processaLinha) {
        Path caminho = pasta.resolve(arquivo);
        if (!Files.exists(caminho)) {
            avisos.add("Arquivo ausente: " + arquivo + " (ignorado).");
            return;
        }
        List<String> linhas;
        try {
            linhas = Files.readAllLines(caminho, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            avisos.add("Não foi possível ler " + arquivo + ": " + ex.getMessage());
            return;
        }
        for (int i = 1; i < linhas.size(); i++) { // linha 0 é o cabeçalho
            if (linhas.get(i).isBlank()) continue;
            try {
                processaLinha.accept(Csv.dividir(linhas.get(i)));
            } catch (RuntimeException ex) {
                avisos.add(arquivo + ", linha " + (i + 1) + " ignorada (" + motivo(ex) + ").");
            }
        }
    }

    private static String motivo(RuntimeException ex) {
        if (ex instanceof RegraDeNegocioException) return ex.getMessage();
        if (ex instanceof IndexOutOfBoundsException) return "faltam colunas";
        if (ex instanceof NumberFormatException) return "número inválido";
        if (ex instanceof java.time.DateTimeException) return "data ou hora inválida";
        if (ex instanceof IllegalArgumentException) return "valor não reconhecido";
        return "dado inválido";
    }

    private static Curso curso(Escola e, String nome) {
        return e.getCursos().stream().filter(c -> c.getNome().equalsIgnoreCase(nome)).findFirst()
                .orElseThrow(() -> new RegraDeNegocioException("curso inexistente: " + nome));
    }

    private static <T> T buscar(Map<Integer, T> mapa, String id, String o) {
        T achado = mapa.get(Integer.parseInt(id));
        if (achado == null) throw new RegraDeNegocioException(o + " " + id + " inexistente");
        return achado;
    }
}
