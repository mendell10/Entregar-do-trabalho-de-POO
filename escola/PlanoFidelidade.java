package escola;

import java.time.LocalDate;
import java.time.Period;

/** Plano de fidelidade: 5% de desconto na mensalidade por ano completo de matrícula, até 15%. */
public final class PlanoFidelidade {
    private static final int DESCONTO_POR_ANO = 5;
    private static final int DESCONTO_MAXIMO = 15;

    private PlanoFidelidade() {}

    /** @return desconto em percentual inteiro (0, 5, 10 ou 15). */
    public static int percentualDesconto(LocalDate dataMatricula, LocalDate hoje) {
        int anosCompletos = Period.between(dataMatricula, hoje).getYears();
        return Math.max(0, Math.min(anosCompletos * DESCONTO_POR_ANO, DESCONTO_MAXIMO));
    }
}
