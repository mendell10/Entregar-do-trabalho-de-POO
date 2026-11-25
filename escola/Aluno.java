package escola;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Aluno da escola. Pode ser VIP (estúdio, desconto em aluguel, reserva em recitais). */
public class Aluno extends Pessoa {
    private final LocalDate dataMatricula;
    private boolean vip;
    private final List<Desempenho> desempenhos = new ArrayList<>();

    public Aluno(int id, String nome, boolean vip, LocalDate dataMatricula) {
        super(id, nome);
        if (dataMatricula == null) throw new RegraDeNegocioException("Informe a data de matrícula.");
        this.vip = vip;
        this.dataMatricula = dataMatricula;
    }

    public LocalDate getDataMatricula() { return dataMatricula; }
    public boolean isVip() { return vip; }

    /** Somente a Escola muda o status VIP (acesso restrito ao pacote). */
    void definirVip(boolean vip) { this.vip = vip; }

    /** Visão somente leitura: ninguém de fora altera a lista diretamente. */
    public List<Desempenho> getDesempenhos() { return Collections.unmodifiableList(desempenhos); }

    void adicionarDesempenho(Desempenho d) { desempenhos.add(d); }

    @Override
    public String getPapel() { return vip ? "Aluno VIP" : "Aluno"; }

    @Override
    public String descricaoDetalhada() {
        return this + " - matrícula em " + dataMatricula + ", " + desempenhos.size() + " registro(s) de desempenho";
    }

    @Override
    public String toString() { return getNome() + (vip ? " [VIP]" : ""); }
}
