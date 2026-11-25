package escola;

/** Problema ao ler ou gravar arquivos (pasta sem permissão, disco cheio etc.). */
public class PersistenciaException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
