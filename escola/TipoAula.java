package escola;

import java.math.BigDecimal;

/** Tipos de aula e suas regras: mensalidade e limite de alunos. */
public enum TipoAula {
    INDIVIDUAL("Individual", Dinheiro.de("280.00"), 1),
    EM_GRUPO("Em grupo", Dinheiro.de("160.00"), 6);

    private final String descricao;
    private final BigDecimal mensalidade;
    private final int limiteAlunos;

    TipoAula(String descricao, BigDecimal mensalidade, int limiteAlunos) {
        this.descricao = descricao;
        this.mensalidade = mensalidade;
        this.limiteAlunos = limiteAlunos;
    }

    public String getDescricao() { return descricao; }
    public BigDecimal getMensalidade() { return mensalidade; }
    public int getLimiteAlunos() { return limiteAlunos; }
}
