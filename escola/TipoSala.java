package escola;

import java.math.BigDecimal;

/** Tipos de sala. Só PRATICA e ESTUDIO podem ser alugadas; ESTUDIO é exclusivo para VIP. */
public enum TipoSala {
    AULA("Sala de aula", Dinheiro.de("0.00"), false, false),
    PRATICA("Sala de prática", Dinheiro.de("30.00"), true, false),
    ESTUDIO("Estúdio de gravação", Dinheiro.de("90.00"), true, true);

    private final String descricao;
    private final BigDecimal precoHora;
    private final boolean alugavel;
    private final boolean exclusivoVip;

    TipoSala(String descricao, BigDecimal precoHora, boolean alugavel, boolean exclusivoVip) {
        this.descricao = descricao;
        this.precoHora = precoHora;
        this.alugavel = alugavel;
        this.exclusivoVip = exclusivoVip;
    }

    public String getDescricao() { return descricao; }
    public BigDecimal getPrecoHora() { return precoHora; }
    public boolean isAlugavel() { return alugavel; }
    public boolean isExclusivoVip() { return exclusivoVip; }
}
