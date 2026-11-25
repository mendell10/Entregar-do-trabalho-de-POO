package escola;

/** Relatório dos recitais: ocupação, reserva VIP e lista de inscritos. */
public class RelatorioEventos implements Relatorio {
    private final Escola escola;

    public RelatorioEventos(Escola escola) { this.escola = escola; }

    @Override
    public String getTitulo() { return "Relatório de recitais e eventos"; }

    @Override
    public String gerar() {
        StringBuilder sb = new StringBuilder("=== " + getTitulo() + " ===\n");
        if (escola.getEventos().isEmpty()) return sb.append("Nenhum evento agendado.\n").toString();
        for (Evento e : escola.getEventos()) {
            long vip = e.getInscritos().stream().filter(Aluno::isVip).count();
            sb.append(String.format("%s%n   ocupação %d/%d | reservadas p/ VIP: %d | inscritos VIP: %d, não VIP: %d%n",
                    e, e.getInscritos().size(), e.getVagasTotais(), e.getVagasReservadasVip(),
                    vip, e.getInscritos().size() - vip));
            sb.append("   inscritos: ").append(e.getInscritos().isEmpty() ? "(ninguém)" : e.getInscritos()).append('\n');
        }
        return sb.toString();
    }
}
