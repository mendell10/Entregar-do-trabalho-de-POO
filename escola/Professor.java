package escola;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/** Professor habilitado a lecionar um ou mais cursos. */
public class Professor extends Pessoa {
    private final Set<Curso> cursosHabilitados = new LinkedHashSet<>();

    public Professor(int id, String nome, Set<Curso> cursos) {
        super(id, nome);
        if (cursos == null || cursos.isEmpty()) throw new RegraDeNegocioException("O professor precisa lecionar ao menos um curso.");
        this.cursosHabilitados.addAll(cursos);
    }

    public Set<Curso> getCursosHabilitados() { return Collections.unmodifiableSet(cursosHabilitados); }
    public boolean leciona(Curso curso) { return cursosHabilitados.contains(curso); }

    @Override
    public String getPapel() { return "Professor"; }

    @Override
    public String descricaoDetalhada() { return getNome() + " - leciona " + cursosHabilitados; }
}
