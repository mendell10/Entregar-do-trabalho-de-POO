package escola;

import java.util.ArrayList;
import java.util.List;

/** Utilitário de CSV com ponto e vírgula. Escapa ';', '\' e quebras de linha dentro do texto. */
final class Csv {
    private Csv() {}

    static String linha(Object... campos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) sb.append(';');
            sb.append(escapar(campos[i] == null ? "" : String.valueOf(campos[i])));
        }
        return sb.toString();
    }

    private static String escapar(String s) {
        return s.replace("\\", "\\\\").replace(";", "\\;").replace("\r", "").replace("\n", "\\n");
    }

    static List<String> dividir(String linha) {
        List<String> campos = new ArrayList<>();
        StringBuilder atual = new StringBuilder();
        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (c == '\\' && i + 1 < linha.length()) {
                char prox = linha.charAt(++i);
                atual.append(prox == 'n' ? '\n' : prox);
            } else if (c == ';') {
                campos.add(atual.toString());
                atual.setLength(0);
            } else {
                atual.append(c);
            }
        }
        campos.add(atual.toString());
        return campos;
    }
}
