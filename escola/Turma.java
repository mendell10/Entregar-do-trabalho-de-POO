package escola;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Turma de um curso: individual (1 aluno) ou em grupo (até 6 alunos). */
public class Turma {
    private final int id;
    private final Curso curso;
    private final TipoAula tipo;
    private final Professor professor;
    private final Sala sala;
    private final Horario horario;
    private final List<Aluno> alunos = new ArrayList<>();

    Turma(int id, Curso curso, TipoAula tipo, Professor professor, Sala sala, Horario horario) {
        this.id = id;
        this.curso = curso;
        this.tipo = tipo;
        this.professor = professor;
        this.sala = sala;
        this.horario = horario;
    }

    public int getId() { return id; }
    public Curso getCurso() { return curso; }
    public TipoAula getTipo() { return tipo; }
    public Professor getProfessor() { return professor; }
    public Sala getSala() { return sala; }
    public Horario getHorario() { return horario; }
    public List<Aluno> getAlunos() { return Collections.unmodifiableList(alunos); }
    public int getVagasDisponiveis() { return tipo.getLimiteAlunos() - alunos.size(); }
    public BigDecimal getMensalidadeBase() { return tipo.getMensalidade(); }

    void adicionarAluno(Aluno aluno) {
        if (alunos.contains(aluno)) {
            throw new RegraDeNegocioException(aluno.getNome() + " já está matriculado nesta turma.");
        }
        if (getVagasDisponiveis() <= 0) {
            throw new RegraDeNegocioException("Turma lotada (limite de " + tipo.getLimiteAlunos() + " aluno(s)).");
        }
        alunos.add(aluno);
    }

    void removerAluno(Aluno aluno) {
        if (!alunos.remove(aluno)) {
            throw new RegraDeNegocioException(aluno.getNome() + " não está matriculado nesta turma.");
        }
    }

    @Override
    public String toString() {
        return "Turma " + id + " - " + curso + " (" + tipo.getDescricao() + ") com " + professor
                + ", " + sala.getNome() + ", " + horario;
    }
}
