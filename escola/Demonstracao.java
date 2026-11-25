package escola;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

/** Demonstração das regras da escola "Nota Máxima". */
public class Demonstracao {
    public static void main(String[] args) {
        LocalDate hoje = LocalDate.of(2026, 11, 23);
        Escola escola = new Escola("Nota Máxima");

        Curso piano = escola.cadastrarCurso("Piano");
        Curso violao = escola.cadastrarCurso("Violão");
        Curso canto = escola.cadastrarCurso("Canto");

        Professor ana = escola.cadastrarProfessor("Ana", Set.of(piano, canto));
        Professor bruno = escola.cadastrarProfessor("Bruno", Set.of(violao));

        Sala sala1 = escola.cadastrarSala("Sala 1", TipoSala.AULA);
        Sala pratica = escola.cadastrarSala("Prática A", TipoSala.PRATICA);
        Sala estudio = escola.cadastrarSala("Estúdio", TipoSala.ESTUDIO);

        Aluno maria = escola.cadastrarAluno("Maria", true, hoje.minusYears(2).minusDays(10)); // VIP, 2 anos
        Aluno joao = escola.cadastrarAluno("João", false, hoje.minusMonths(3));

        titulo("1. Turmas e conflito de horários");
        Turma t1 = escola.criarTurma(piano, TipoAula.INDIVIDUAL, ana, sala1,
                new Horario(DayOfWeek.MONDAY, LocalTime.of(14, 0), LocalTime.of(15, 0)));
        System.out.println(t1);
        tentar("Ana em dois lugares ao mesmo tempo", () -> escola.criarTurma(canto, TipoAula.EM_GRUPO, ana, sala1,
                new Horario(DayOfWeek.MONDAY, LocalTime.of(14, 30), LocalTime.of(15, 30))));

        escola.matricular(maria, t1);
        tentar("Turma individual com 2º aluno", () -> escola.matricular(joao, t1));

        titulo("2. Mensalidade com fidelidade");
        Cobranca mensMaria = escola.gerarMensalidade(maria, t1, hoje);
        System.out.println(mensMaria + "  (280,00 - 10%)");
        Turma t2 = escola.criarTurma(violao, TipoAula.EM_GRUPO, bruno, sala1,
                new Horario(DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(11, 0)));
        escola.matricular(joao, t2);
        System.out.println(escola.gerarMensalidade(joao, t2, hoje));
        escola.registrarPagamento(mensMaria, hoje);
        System.out.println("Pagamentos pendentes de Maria: " + escola.cobrancasPendentes(maria).size());

        titulo("3. Aluguel de salas e estúdios");
        LocalDateTime ini = LocalDateTime.of(2026, 11, 24, 18, 0);
        System.out.println(escola.alugarSala(maria, estudio, ini, ini.plusHours(2)) + "  (2h x 90 - 20% VIP)");
        System.out.println(escola.alugarSala(joao, pratica, ini, ini.plusHours(1)) + "  (1h x 30)");
        tentar("Estúdio para aluno não VIP", () -> escola.alugarSala(joao, estudio, ini.plusHours(3), ini.plusHours(4)));
        tentar("Aluguel em horário ocupado", () -> escola.alugarSala(maria, pratica, ini.plusMinutes(30), ini.plusHours(2)));
        tentar("Aluguel de sala de aula (não alugável)", () -> escola.alugarSala(maria, sala1,
                LocalDateTime.of(2026, 11, 23, 14, 30), LocalDateTime.of(2026, 11, 23, 15, 30)));

        titulo("4. Desempenho");
        escola.registrarDesempenho(maria, piano, 8.5, "Boa leitura de partitura", hoje.minusDays(30));
        escola.registrarDesempenho(maria, piano, 9.2, "Dominou a escala de Dó", hoje);
        tentar("Nota inválida", () -> escola.registrarDesempenho(maria, piano, 11, "", hoje));
        System.out.print(escola.relatorioProgresso(maria));

        titulo("5. Recital com vagas reservadas para VIP");
        Evento recital = escola.agendarEvento("Recital de Fim de Ano", LocalDateTime.of(2026, 12, 20, 19, 0), 4);
        // 4 vagas: 2 reservadas para VIP. Hoje faltam 27 dias, a reserva está ativa.
        Aluno a3 = escola.cadastrarAluno("Carlos", false, hoje);
        Aluno a4 = escola.cadastrarAluno("Dora", false, hoje);
        Aluno a5 = escola.cadastrarAluno("Eva", false, hoje);
        escola.inscreverEmEvento(joao, recital, hoje);
        escola.inscreverEmEvento(a3, recital, hoje);
        tentar("3º não VIP antes do prazo (vagas são do VIP)", () -> escola.inscreverEmEvento(a4, recital, hoje));
        escola.inscreverEmEvento(maria, recital, hoje);
        // Depois do prazo (a menos de 7 dias), as vagas restantes ficam livres para todos.
        escola.inscreverEmEvento(a4, recital, LocalDate.of(2026, 12, 14));
        System.out.println(recital);
        tentar("Evento lotado", () -> escola.inscreverEmEvento(a5, recital, LocalDate.of(2026, 12, 14)));
    }

    private static void titulo(String t) { System.out.println("\n--- " + t + " ---"); }

    private static void tentar(String descricao, Runnable acao) {
        try {
            acao.run();
            System.out.println("[ERRO] Deveria ter falhado: " + descricao);
        } catch (RegraDeNegocioException e) {
            System.out.println("Bloqueado (" + descricao + "): " + e.getMessage());
        }
    }
}
