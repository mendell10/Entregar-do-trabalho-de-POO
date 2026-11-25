package escola;

import java.text.Normalizer;

/**
 * Contrato de qualquer relatório da escola. O menu e o exportador trabalham só com
 * esta interface, então um relatório novo entra no sistema sem mexer neles.
 */
public interface Relatorio {
    String getTitulo();

    /** Texto completo do relatório, pronto para imprimir no console ou gravar em arquivo. */
    String gerar();

    /** Nome de arquivo seguro derivado do título (sem acentos nem símbolos). */
    default String nomeArquivo() {
        String semAcento = Normalizer.normalize(getTitulo(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String limpo = semAcento.toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        return limpo.isEmpty() ? "relatorio" : limpo;
    }
}
