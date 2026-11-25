package escola;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Cobrança genérica da escola. Nasce pendente e só muda via pagar().
 * As subclasses (Mensalidade e TaxaAluguel) definem o tipo e a descrição, então a
 * Escola trata todas as cobranças da mesma forma (polimorfismo).
 */
public abstract class Cobranca {
    public enum Tipo { MENSALIDADE, ALUGUEL }

    private final int id;
    private final Aluno aluno;
    private final BigDecimal valor;
    private LocalDate dataPagamento;

    protected Cobranca(int id, Aluno aluno, BigDecimal valor) {
        if (aluno == null) throw new RegraDeNegocioException("A cobrança precisa de um aluno.");
        if (valor == null || valor.signum() < 0) throw new RegraDeNegocioException("Valor de cobrança inválido.");
        this.id = id;
        this.aluno = aluno;
        this.valor = valor;
    }

    public abstract Tipo getTipo();
    public abstract String getDescricao();
    /** Id da turma (mensalidade) ou do aluguel (taxa) que originou a cobrança. */
    public abstract int getReferenciaId();
    /** Texto curto do item cobrado, guardado no arquivo para reconstruir a descrição. */
    public abstract String getDetalhe();
    /** Desconto de fidelidade aplicado, em %. Só a mensalidade tem. */
    public int getDescontoPercentual() { return 0; }

    public int getId() { return id; }
    public Aluno getAluno() { return aluno; }
    public BigDecimal getValor() { return valor; }
    public boolean isPago() { return dataPagamento != null; }
    public LocalDate getDataPagamento() { return dataPagamento; }

    public void pagar(LocalDate data) {
        if (isPago()) throw new RegraDeNegocioException("Pagamento " + id + " já foi quitado.");
        if (data == null) throw new RegraDeNegocioException("Informe a data do pagamento.");
        this.dataPagamento = data;
    }

    @Override
    public String toString() {
        return "#" + id + " " + getTipo() + " - " + getDescricao() + " - " + Dinheiro.formatar(valor)
                + (isPago() ? " (pago em " + dataPagamento + ")" : " (PENDENTE)");
    }
}
