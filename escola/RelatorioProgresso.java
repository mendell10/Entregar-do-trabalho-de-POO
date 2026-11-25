package escola;

import java.util.List;

/** Relatório de progresso de um aluno: notas, média e última nota por curso. */
public class RelatorioProgresso implements Relatorio {
    private final Escola escola;
    private final Aluno aluno;

    public RelatorioProgresso(Escola escola, Aluno aluno) {
        this.escola = escola;
        this.aluno = aluno;
    }

    @Override
    public String getTitulo() { return "Relatório de progresso: " + aluno; }

    @Override
    public String gerar() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(getTitulo()).append(" ===\n");
        if (aluno.getDesempenhos().isEmpty()) {
            return sb.append("Nenhum registro de desempenho.\n").toString();
        }
        for (Curso curso : escola.getCursos()) {
            List<Desempenho> lista = aluno.getDesempenhos().stream()
                    .filter(d -> d.getCurso().equals(curso)).toList();
            if (lista.isEmpty()) continue;
            double media = lista.stream().mapToDouble(Desempenho::getNota).average().orElse(0);
            Desempenho ultimo = lista.get(lista.size() - 1);
            sb.append(String.format("%s: %d registro(s), média %.2f, última nota %.1f%n",
                    curso, lista.size(), media, ultimo.getNota()));
            for (Desempenho d : lista) {
                sb.append(String.format("   %s  nota %.1f  %s%n", d.getData(), d.getNota(), d.getObservacao()));
            }
        }
        return sb.toString();
    }
}
