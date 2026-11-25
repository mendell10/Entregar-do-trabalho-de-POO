package escola;

import java.math.BigDecimal;

/** Relatório financeiro da escola: cobrado, pago e pendente por aluno e por tipo de cobrança. */
public class RelatorioFinanceiro implements Relatorio {
    private final Escola escola;

    public RelatorioFinanceiro(Escola escola) { this.escola = escola; }

    @Override
    public String getTitulo() { return "Relatório financeiro"; }

    @Override
    public String gerar() {
        StringBuilder sb = new StringBuilder("=== " + getTitulo() + " ===\n");
        BigDecimal totalCobrado = BigDecimal.ZERO, totalPago = BigDecimal.ZERO;
        for (Aluno aluno : escola.getAlunos()) {
            BigDecimal cobrado = BigDecimal.ZERO, pago = BigDecimal.ZERO;
            for (Cobranca c : escola.getCobrancas()) {
                if (!c.getAluno().equals(aluno)) continue;
                cobrado = cobrado.add(c.getValor());
                if (c.isPago()) pago = pago.add(c.getValor());
            }
            if (cobrado.signum() == 0) continue;
            sb.append(String.format("%-18s cobrado %s | pago %s | pendente %s%n", aluno,
                    Dinheiro.formatar(cobrado), Dinheiro.formatar(pago), Dinheiro.formatar(cobrado.subtract(pago))));
            totalCobrado = totalCobrado.add(cobrado);
            totalPago = totalPago.add(pago);
        }
        if (totalCobrado.signum() == 0) return sb.append("Nenhuma cobrança registrada.\n").toString();
        sb.append("--- Totais ---\n");
        sb.append("Cobrado: ").append(Dinheiro.formatar(totalCobrado)).append('\n');
        sb.append("Recebido: ").append(Dinheiro.formatar(totalPago)).append('\n');
        sb.append("A receber: ").append(Dinheiro.formatar(totalCobrado.subtract(totalPago))).append('\n');
        for (Cobranca.Tipo tipo : Cobranca.Tipo.values()) {
            BigDecimal soma = escola.getCobrancas().stream().filter(c -> c.getTipo() == tipo)
                    .map(Cobranca::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
            sb.append(String.format("  %s: %s%n", tipo, Dinheiro.formatar(soma)));
        }
        return sb.toString();
    }
}
