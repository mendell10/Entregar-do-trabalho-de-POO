package escola;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Grava qualquer Relatorio como arquivo de texto UTF-8. */
public final class ExportadorRelatorio {
    private ExportadorRelatorio() {}

    public static Path salvar(Relatorio relatorio, Path pasta) {
        try {
            Files.createDirectories(pasta);
            Path arquivo = pasta.resolve(relatorio.nomeArquivo() + ".txt");
            Files.writeString(arquivo, relatorio.gerar(), StandardCharsets.UTF_8);
            return arquivo;
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível gravar o relatório em " + pasta + ": " + e.getMessage(), e);
        }
    }
}
