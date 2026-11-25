package escola;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Recital ou apresentação com número de vagas.
 * Metade das vagas fica reservada para VIP até 7 dias antes do evento;
 * depois disso, as vagas restantes são livres para todos.
 */
public class Evento {
    private static final int DIAS_RESERVA_VIP = 7;

    private final int id;
    private String nome;
    private final LocalDateTime dataHora;
    private final int vagasTotais;
    private final List<Aluno> inscritos = new ArrayList<>();

    Evento(int id, String nome, LocalDateTime dataHora, int vagasTotais) {
        if (vagasTotais <= 0) throw new RegraDeNegocioException("O evento precisa ter ao menos 1 vaga.");
        if (dataHora == null) throw new RegraDeNegocioException("Informe a data e a hora do evento.");
        this.id = id;
        this.nome = validarNome(nome);
        this.dataHora = dataHora;
        this.vagasTotais = vagasTotais;
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) throw new RegraDeNegocioException("O evento precisa ter um nome.");
        return nome.trim();
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public LocalDateTime getDataHora() { return dataHora; }
    public int getVagasTotais() { return vagasTotais; }
    public List<Aluno> getInscritos() { return Collections.unmodifiableList(inscritos); }
    public int getVagasReservadasVip() { return vagasTotais / 2; }
    public int getVagasLivres() { return vagasTotais - inscritos.size(); }

    private boolean reservaVipAtiva(LocalDate hoje) {
        return !hoje.isAfter(dataHora.toLocalDate().minusDays(DIAS_RESERVA_VIP));
    }

    void inscrever(Aluno aluno, LocalDate hoje) {
        if (hoje.isAfter(dataHora.toLocalDate())) {
            throw new RegraDeNegocioException("O evento já aconteceu.");
        }
        if (inscritos.contains(aluno)) {
            throw new RegraDeNegocioException(aluno.getNome() + " já está inscrito neste evento.");
        }
        if (getVagasLivres() <= 0) {
            throw new RegraDeNegocioException("Evento lotado.");
        }
        if (!aluno.isVip() && reservaVipAtiva(hoje)) {
            long naoVip = inscritos.stream().filter(a -> !a.isVip()).count();
            int limiteNaoVip = vagasTotais - getVagasReservadasVip();
            if (naoVip >= limiteNaoVip) {
                throw new RegraDeNegocioException("As vagas restantes estão reservadas para alunos VIP até "
                        + DIAS_RESERVA_VIP + " dias antes do evento.");
            }
        }
        inscritos.add(aluno);
    }

    void renomear(String novoNome) { this.nome = validarNome(novoNome); }

    void cancelarInscricao(Aluno aluno) {
        if (!inscritos.remove(aluno)) {
            throw new RegraDeNegocioException(aluno.getNome() + " não está inscrito neste evento.");
        }
    }

    /** Usado só ao recarregar do arquivo: a inscrição já foi validada quando aconteceu. */
    void restaurarInscrito(Aluno aluno) {
        if (inscritos.size() >= vagasTotais) throw new RegraDeNegocioException("Mais inscritos do que vagas no evento " + id + ".");
        inscritos.add(aluno);
    }

    @Override
    public String toString() {
        return "Evento " + id + " - " + nome + " em " + dataHora + " (" + inscritos.size() + "/" + vagasTotais + " vagas)";
    }
}
