package escola;

import java.math.BigDecimal;

/** Mensalidade de uma turma, já com o desconto do plano de fidelidade aplicado. */
public final class Mensalidade extends Cobranca {
    private final int turmaId;
    private final String detalheTurma;
    private final int descontoPercentual;

    Mensalidade(int id, Aluno aluno, int turmaId, String detalheTurma, int descontoPercentual, BigDecimal valor) {
        super(id, aluno, valor);
        this.turmaId = turmaId;
        this.detalheTurma = detalheTurma;
        this.descontoPercentual = descontoPercentual;
    }

    @Override public Tipo getTipo() { return Tipo.MENSALIDADE; }
    @Override public int getReferenciaId() { return turmaId; }
    @Override public String getDetalhe() { return detalheTurma; }
    @Override public int getDescontoPercentual() { return descontoPercentual; }

    @Override
    public String getDescricao() {
        return "Mensalidade " + detalheTurma + (descontoPercentual > 0 ? ", fidelidade -" + descontoPercentual + "%" : "");
    }
}
