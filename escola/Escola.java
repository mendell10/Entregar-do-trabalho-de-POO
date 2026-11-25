package escola;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Fachada do sistema da escola "Nota Máxima". Toda a coleção de dados é privada:
 * o mundo externo só age por meio dos métodos abaixo, que aplicam as regras de negócio.
 */
public class Escola {
    private final String nome;
    private final List<Curso> cursos = new ArrayList<>();
    private final List<Aluno> alunos = new ArrayList<>();
    private final List<Professor> professores = new ArrayList<>();
    private final List<Sala> salas = new ArrayList<>();
    private final List<Turma> turmas = new ArrayList<>();
    private final List<Aluguel> alugueis = new ArrayList<>();
    private final List<Evento> eventos = new ArrayList<>();
    private final List<Cobranca> cobrancas = new ArrayList<>();
    private int proximoId = 1;

    public Escola(String nome) {
        this.nome = nome;
    }

    public String getNome() { return nome; }
    public List<Curso> getCursos() { return Collections.unmodifiableList(cursos); }
    public List<Aluno> getAlunos() { return Collections.unmodifiableList(alunos); }
    public List<Professor> getProfessores() { return Collections.unmodifiableList(professores); }
    public List<Sala> getSalas() { return Collections.unmodifiableList(salas); }
    public List<Turma> getTurmas() { return Collections.unmodifiableList(turmas); }
    public List<Aluguel> getAlugueis() { return Collections.unmodifiableList(alugueis); }
    public List<Evento> getEventos() { return Collections.unmodifiableList(eventos); }
    public List<Cobranca> getCobrancas() { return Collections.unmodifiableList(cobrancas); }

    /** Professores e alunos como Pessoa: quem usa a lista não precisa saber o tipo concreto. */
    public List<Pessoa> getPessoas() {
        List<Pessoa> todas = new ArrayList<>(professores);
        todas.addAll(alunos);
        return Collections.unmodifiableList(todas);
    }

    public boolean estaVazia() {
        return alunos.isEmpty() && professores.isEmpty() && salas.isEmpty() && turmas.isEmpty()
                && alugueis.isEmpty() && eventos.isEmpty() && cobrancas.isEmpty();
    }

    private int novoId() { return proximoId++; }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) throw new RegraDeNegocioException(mensagem);
    }

    // ---------- 1. Cadastros ----------

    public Curso cadastrarCurso(String nomeCurso) {
        Curso curso = new Curso(nomeCurso);
        exigir(!cursos.contains(curso), "Curso já cadastrado: " + curso);
        cursos.add(curso);
        return curso;
    }

    public Aluno cadastrarAluno(String nomeAluno, boolean vip, LocalDate dataMatricula) {
        Aluno aluno = new Aluno(novoId(), nomeAluno, vip, dataMatricula);
        alunos.add(aluno);
        return aluno;
    }

    public Professor cadastrarProfessor(String nomeProfessor, Set<Curso> cursosQueLeciona) {
        for (Curso c : cursosQueLeciona) {
            exigir(cursos.contains(c), "Curso não cadastrado: " + c);
        }
        Professor p = new Professor(novoId(), nomeProfessor, cursosQueLeciona);
        professores.add(p);
        return p;
    }

    public Sala cadastrarSala(String nomeSala, TipoSala tipo) {
        Sala sala = new Sala(novoId(), nomeSala, tipo);
        salas.add(sala);
        return sala;
    }

    // ---------- 1b. Edição ----------

    public void editarAluno(Aluno aluno, String novoNome, boolean vip) {
        exigir(alunos.contains(aluno), "Aluno não cadastrado nesta escola.");
        aluno.renomear(novoNome);
        aluno.definirVip(vip);
    }

    public void editarProfessor(Professor professor, String novoNome) {
        exigir(professores.contains(professor), "Professor não cadastrado nesta escola.");
        professor.renomear(novoNome);
    }

    public void editarSala(Sala sala, String novoNome) {
        exigir(salas.contains(sala), "Sala não cadastrada nesta escola.");
        sala.renomear(novoNome);
    }

    public void editarEvento(Evento evento, String novoNome) {
        exigir(eventos.contains(evento), "Evento não cadastrado nesta escola.");
        evento.renomear(novoNome);
    }

    // ---------- 1c. Remoção (sempre respeitando as regras) ----------

    public void removerCurso(Curso curso) {
        exigir(cursos.contains(curso), "Curso não cadastrado nesta escola.");
        exigir(professores.stream().noneMatch(p -> p.leciona(curso)),
                "Há professor habilitado em " + curso + ". Não é possível remover.");
        exigir(turmas.stream().noneMatch(t -> t.getCurso().equals(curso)),
                "Há turma de " + curso + ". Remova as turmas antes.");
        exigir(alunos.stream().flatMap(a -> a.getDesempenhos().stream()).noneMatch(d -> d.getCurso().equals(curso)),
                "Há notas registradas em " + curso + ". Não é possível remover.");
        cursos.remove(curso);
    }

    public void removerProfessor(Professor professor) {
        exigir(professores.contains(professor), "Professor não cadastrado nesta escola.");
        exigir(turmas.stream().noneMatch(t -> t.getProfessor().equals(professor)),
                professor.getNome() + " ainda tem turmas. Remova as turmas antes.");
        professores.remove(professor);
    }

    public void removerSala(Sala sala) {
        exigir(salas.contains(sala), "Sala não cadastrada nesta escola.");
        exigir(turmas.stream().noneMatch(t -> t.getSala().equals(sala)),
                sala.getNome() + " é usada por uma turma. Remova a turma antes.");
        exigir(alugueis.stream().noneMatch(a -> a.getSala().equals(sala)),
                sala.getNome() + " tem aluguéis. Cancele-os antes.");
        salas.remove(sala);
    }

    public void removerTurma(Turma turma) {
        exigir(turmas.contains(turma), "Turma não cadastrada nesta escola.");
        exigir(turma.getAlunos().isEmpty(), "A turma ainda tem alunos matriculados. Desmatricule-os antes.");
        turmas.remove(turma);
    }

    public void removerEvento(Evento evento) {
        exigir(eventos.contains(evento), "Evento não cadastrado nesta escola.");
        eventos.remove(evento);
    }

    /** Remove o aluno e tudo o que é dele, mas só se ele não deve nada à escola. */
    public void removerAluno(Aluno aluno) {
        exigir(alunos.contains(aluno), "Aluno não cadastrado nesta escola.");
        exigir(cobrancasPendentes(aluno).isEmpty(),
                aluno.getNome() + " tem cobranças pendentes. Quite-as antes de remover.");
        turmas.forEach(t -> { if (t.getAlunos().contains(aluno)) t.removerAluno(aluno); });
        eventos.forEach(e -> { if (e.getInscritos().contains(aluno)) e.cancelarInscricao(aluno); });
        alugueis.removeIf(a -> a.getAluno().equals(aluno));
        cobrancas.removeIf(c -> c.getAluno().equals(aluno));
        alunos.remove(aluno);
    }

    // ---------- 2. Turmas e horários (sem conflito de professor, sala ou aluno) ----------

    public Turma criarTurma(Curso curso, TipoAula tipo, Professor professor, Sala sala, Horario horario) {
        exigir(cursos.contains(curso), "Curso não cadastrado: " + curso);
        exigir(professor.leciona(curso), professor.getNome() + " não leciona " + curso + ".");
        exigir(sala.getTipo() == TipoSala.AULA, sala.getNome() + " não é uma sala de aula.");
        for (Turma t : turmas) {
            if (t.getHorario().conflitaCom(horario)) {
                exigir(!t.getProfessor().equals(professor),
                        "Conflito: " + professor.getNome() + " já tem aula em " + t.getHorario() + ".");
                exigir(!t.getSala().equals(sala),
                        "Conflito: " + sala.getNome() + " já está ocupada em " + t.getHorario() + ".");
            }
        }
        Turma turma = new Turma(novoId(), curso, tipo, professor, sala, horario);
        turmas.add(turma);
        return turma;
    }

    public void matricular(Aluno aluno, Turma turma) {
        exigir(alunos.contains(aluno), "Aluno não cadastrado nesta escola.");
        exigir(turmas.contains(turma), "Turma não cadastrada nesta escola.");
        for (Turma outra : turmas) {
            if (outra != turma && outra.getAlunos().contains(aluno) && outra.getHorario().conflitaCom(turma.getHorario())) {
                throw new RegraDeNegocioException("Conflito: " + aluno.getNome() + " já tem aula em " + outra.getHorario() + ".");
            }
        }
        turma.adicionarAluno(aluno);
    }

    public void desmatricular(Aluno aluno, Turma turma) {
        exigir(turmas.contains(turma), "Turma não cadastrada nesta escola.");
        turma.removerAluno(aluno);
    }

    // ---------- 5. Mensalidade com plano de fidelidade ----------

    public BigDecimal calcularMensalidade(Aluno aluno, Turma turma, LocalDate hoje) {
        int desconto = PlanoFidelidade.percentualDesconto(aluno.getDataMatricula(), hoje);
        BigDecimal fator = BigDecimal.valueOf(100 - desconto).divide(BigDecimal.valueOf(100));
        return Dinheiro.arredondar(turma.getMensalidadeBase().multiply(fator));
    }

    public Mensalidade gerarMensalidade(Aluno aluno, Turma turma, LocalDate hoje) {
        exigir(turma.getAlunos().contains(aluno), aluno.getNome() + " não está matriculado nesta turma.");
        boolean jaPendente = cobrancas.stream().anyMatch(c -> c instanceof Mensalidade m
                && m.getAluno().equals(aluno) && m.getReferenciaId() == turma.getId() && !m.isPago());
        exigir(!jaPendente, "Já existe uma mensalidade pendente de " + aluno.getNome() + " para esta turma.");
        int desconto = PlanoFidelidade.percentualDesconto(aluno.getDataMatricula(), hoje);
        String detalhe = turma.getCurso() + " (" + turma.getTipo().getDescricao() + ")";
        Mensalidade m = new Mensalidade(novoId(), aluno, turma.getId(), detalhe, desconto,
                calcularMensalidade(aluno, turma, hoje));
        cobrancas.add(m);
        return m;
    }

    // ---------- 2. Aluguel de salas e estúdios ----------

    public Aluguel alugarSala(Aluno aluno, Sala sala, LocalDateTime inicio, LocalDateTime fim) {
        exigir(alunos.contains(aluno), "Aluno não cadastrado nesta escola.");
        TipoSala tipo = sala.getTipo();
        exigir(tipo.isAlugavel(), sala.getNome() + " não está disponível para aluguel.");
        exigir(!tipo.isExclusivoVip() || aluno.isVip(), "Estúdios de gravação são exclusivos para alunos VIP.");
        exigir(fim.isAfter(inicio) && inicio.toLocalDate().equals(fim.toLocalDate()),
                "O aluguel deve começar e terminar no mesmo dia, com fim depois do início.");
        for (Aluguel a : alugueis) {
            exigir(!(a.getSala().equals(sala) && a.conflitaCom(inicio, fim)), sala.getNome() + " já está alugada nesse horário.");
        }
        BigDecimal horas = BigDecimal.valueOf(ChronoUnit.MINUTES.between(inicio, fim))
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        BigDecimal valor = tipo.getPrecoHora().multiply(horas);
        if (aluno.isVip()) {
            valor = valor.multiply(new BigDecimal("0.80")); // 20% de desconto VIP
        }
        Aluguel aluguel = new Aluguel(novoId(), aluno, sala, inicio, fim, Dinheiro.arredondar(valor));
        alugueis.add(aluguel);
        cobrancas.add(new TaxaAluguel(novoId(), aluno, aluguel.getId(), sala.getNome(), aluguel.getValor()));
        return aluguel;
    }

    /** Cancela o aluguel e a taxa pendente dele; se a taxa já foi paga, o cancelamento é bloqueado. */
    public void cancelarAluguel(Aluguel aluguel) {
        exigir(alugueis.contains(aluguel), "Aluguel não cadastrado nesta escola.");
        Cobranca taxa = cobrancas.stream()
                .filter(c -> c instanceof TaxaAluguel t && t.getAluguelId() == aluguel.getId())
                .findFirst().orElse(null);
        exigir(taxa == null || !taxa.isPago(), "O aluguel já foi pago e não pode ser cancelado.");
        if (taxa != null) cobrancas.remove(taxa);
        alugueis.remove(aluguel);
    }

    // ---------- 3. Desempenho ----------

    public void registrarDesempenho(Aluno aluno, Curso curso, double nota, String observacao, LocalDate data) {
        exigir(alunos.contains(aluno), "Aluno não cadastrado nesta escola.");
        exigir(cursos.contains(curso), "Curso não cadastrado: " + curso);
        aluno.adicionarDesempenho(new Desempenho(curso, nota, observacao, data));
    }

    public String relatorioProgresso(Aluno aluno) {
        return new RelatorioProgresso(this, aluno).gerar();
    }

    // ---------- 4. Recitais e eventos ----------

    public Evento agendarEvento(String nomeEvento, LocalDateTime dataHora, int vagas) {
        Evento e = new Evento(novoId(), nomeEvento, dataHora, vagas);
        eventos.add(e);
        return e;
    }

    public void inscreverEmEvento(Aluno aluno, Evento evento, LocalDate hoje) {
        exigir(alunos.contains(aluno), "Aluno não cadastrado nesta escola.");
        exigir(eventos.contains(evento), "Evento não cadastrado nesta escola.");
        evento.inscrever(aluno, hoje);
    }

    public void cancelarInscricao(Aluno aluno, Evento evento) {
        exigir(eventos.contains(evento), "Evento não cadastrado nesta escola.");
        evento.cancelarInscricao(aluno);
    }

    // ---------- 5. Pagamentos ----------

    public void registrarPagamento(Cobranca cobranca, LocalDate data) {
        exigir(cobrancas.contains(cobranca), "Cobrança não cadastrada nesta escola.");
        cobranca.pagar(data);
    }

    public List<Cobranca> cobrancasPendentes(Aluno aluno) {
        return cobrancas.stream().filter(c -> c.getAluno().equals(aluno) && !c.isPago()).toList();
    }

    // ---------- Apoio à persistência (acesso restrito ao pacote) ----------

    int getProximoId() { return proximoId; }
    void definirProximoId(int valor) { this.proximoId = Math.max(1, valor); }

    /** Apaga todos os dados (usado ao recarregar de arquivo ou ao zerar a escola). */
    public void limparTudo() {
        cursos.clear(); alunos.clear(); professores.clear(); salas.clear();
        turmas.clear(); alugueis.clear(); eventos.clear(); cobrancas.clear();
        proximoId = 1;
    }

    void restaurar(Curso c) { cursos.add(c); }
    void restaurar(Professor p) { professores.add(p); }
    void restaurar(Aluno a) { alunos.add(a); }
    void restaurar(Sala s) { salas.add(s); }
    void restaurar(Turma t) { turmas.add(t); }
    void restaurar(Aluguel a) { alugueis.add(a); }
    void restaurar(Evento e) { eventos.add(e); }
    void restaurar(Cobranca c) { cobrancas.add(c); }
}
