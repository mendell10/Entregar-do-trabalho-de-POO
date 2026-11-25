package escola;

import java.time.DayOfWeek;
import java.time.LocalTime;

/** Horário semanal imutável (dia da semana + hora inicial e final). */
public final class Horario {
    private final DayOfWeek dia;
    private final LocalTime inicio;
    private final LocalTime fim;

    public Horario(DayOfWeek dia, LocalTime inicio, LocalTime fim) {
        if (dia == null || inicio == null || fim == null || !fim.isAfter(inicio)) {
            throw new RegraDeNegocioException("Horário inválido: o fim deve ser depois do início.");
        }
        this.dia = dia;
        this.inicio = inicio;
        this.fim = fim;
    }

    public DayOfWeek getDia() { return dia; }
    public LocalTime getInicio() { return inicio; }
    public LocalTime getFim() { return fim; }

    /** Dois horários conflitam se são no mesmo dia e os intervalos se sobrepõem. */
    public boolean conflitaCom(Horario outro) {
        return dia == outro.dia && inicio.isBefore(outro.fim) && outro.inicio.isBefore(fim);
    }

    @Override
    public String toString() { return dia + " " + inicio + "-" + fim; }
}
