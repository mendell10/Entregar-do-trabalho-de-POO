package escola;

import java.math.BigDecimal;

/** Taxa cobrada pelo aluguel de uma sala de prática ou estúdio. */
public final class TaxaAluguel extends Cobranca {
    private final int aluguelId;
    private final String nomeSala;

    TaxaAluguel(int id, Aluno aluno, int aluguelId, String nomeSala, BigDecimal valor) {
        super(id, aluno, valor);
        this.aluguelId = aluguelId;
        this.nomeSala = nomeSala;
    }

    public int getAluguelId() { return aluguelId; }

    @Override public Tipo getTipo() { return Tipo.ALUGUEL; }
    @Override public int getReferenciaId() { return aluguelId; }
    @Override public String getDetalhe() { return nomeSala; }
    @Override public String getDescricao() { return "Aluguel " + nomeSala; }
}
