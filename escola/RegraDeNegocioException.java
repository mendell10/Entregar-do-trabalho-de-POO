package escola;

/** Lançada sempre que uma regra de negócio da escola é violada. */
public class RegraDeNegocioException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
