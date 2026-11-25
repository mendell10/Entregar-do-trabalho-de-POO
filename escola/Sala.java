package escola;

public class Sala {
    private final int id;
    private String nome;
    private final TipoSala tipo;

    public Sala(int id, String nome, TipoSala tipo) {
        if (tipo == null) throw new RegraDeNegocioException("Informe o tipo da sala.");
        this.id = id;
        this.nome = validarNome(nome);
        this.tipo = tipo;
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) throw new RegraDeNegocioException("A sala precisa ter um nome.");
        return nome.trim();
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public TipoSala getTipo() { return tipo; }

    void renomear(String novoNome) { this.nome = validarNome(novoNome); }

    @Override
    public String toString() { return nome + " (" + tipo.getDescricao() + ")"; }
}
