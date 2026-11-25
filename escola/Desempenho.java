package escola;

import java.time.LocalDate;

/** Registro imutável de nota/progresso de um aluno em um curso. */
public final class Desempenho {
    private final Curso curso;
    private final double nota;
    private final String observacao;
    private final LocalDate data;

    public Desempenho(Curso curso, double nota, String observacao, LocalDate data) {
        if (curso == null) throw new RegraDeNegocioException("Informe o curso.");
        if (nota < 0 || nota > 10) throw new RegraDeNegocioException("A nota deve estar entre 0 e 10.");
        this.curso = curso;
        this.nota = nota;
        this.observacao = observacao == null ? "" : observacao;
        this.data = data;
    }

    public Curso getCurso() { return curso; }
    public double getNota() { return nota; }
    public String getObservacao() { return observacao; }
    public LocalDate getData() { return data; }
}
