package escola;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class Aluguel {
    private final int id;
    private final Aluno aluno;
    private final Sala sala;
    private final LocalDateTime inicio;
    private final LocalDateTime fim;
    private final BigDecimal valor;

    Aluguel(int id, Aluno aluno, Sala sala, LocalDateTime inicio, LocalDateTime fim, BigDecimal valor) {
        this.id = id;
        this.aluno = aluno;
        this.sala = sala;
        this.inicio = inicio;
        this.fim = fim;
        this.valor = valor;
    }

    public int getId() { return id; }
    public Aluno getAluno() { return aluno; }
    public Sala getSala() { return sala; }
    public LocalDateTime getInicio() { return inicio; }
    public LocalDateTime getFim() { return fim; }
    public BigDecimal getValor() { return valor; }

    boolean conflitaCom(LocalDateTime outroInicio, LocalDateTime outroFim) {
        return inicio.isBefore(outroFim) && outroInicio.isBefore(fim);
    }

    @Override
    public String toString() {
        return "Aluguel " + id + " - " + sala.getNome() + " para " + aluno.getNome()
                + " de " + inicio + " a " + fim + " = " + Dinheiro.formatar(valor);
    }
}
