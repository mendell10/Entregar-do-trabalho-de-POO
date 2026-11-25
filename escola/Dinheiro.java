package escola;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/** Utilitário para trabalhar com valores monetários (sempre 2 casas, arredondamento HALF_UP). */
public final class Dinheiro {
    private Dinheiro() {}

    public static BigDecimal de(String valor) {
        return new BigDecimal(valor).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal arredondar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    public static String formatar(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(valor);
    }
}
