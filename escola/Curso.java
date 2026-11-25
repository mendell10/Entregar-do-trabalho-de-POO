package escola;

import java.util.Objects;

public class Curso {
    private final String nome;

    public Curso(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("O curso precisa ter um nome.");
        }
        this.nome = nome.trim();
    }

    public String getNome() { return nome; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Curso c && c.nome.equalsIgnoreCase(nome);
    }

    @Override
    public int hashCode() { return Objects.hash(nome.toLowerCase()); }

    @Override
    public String toString() { return nome; }
}
