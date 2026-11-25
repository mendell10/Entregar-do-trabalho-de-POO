package escola;

import java.nio.file.Path;

/**
 * Ponto de entrada. Uso: java escola.Main [pasta-de-dados]
 * Sem argumento, os dados ficam na pasta "dados" (criada automaticamente).
 */
public class Main {
    public static void main(String[] args) {
        Path pasta = Path.of(args.length > 0 ? args[0] : "dados");
        new Menu(new RepositorioCsv(pasta)).executar();
    }
}
