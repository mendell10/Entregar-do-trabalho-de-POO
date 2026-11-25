package escola;

/**
 * Abstração comum a todas as pessoas da escola (aluno e professor).
 * Concentra identidade e nome (com validação) em um só lugar; cada subclasse
 * define o seu papel e a sua descrição detalhada (polimorfismo).
 */
public abstract class Pessoa {
    private final int id;
    private String nome;

    protected Pessoa(int id, String nome) {
        this.id = id;
        this.nome = validarNome(nome);
    }

    private static String validarNome(String nome) {
        if (nome == null || nome.isBlank()) throw new RegraDeNegocioException("O nome não pode ficar em branco.");
        return nome.trim();
    }

    public int getId() { return id; }
    public String getNome() { return nome; }

    /** Só a Escola renomeia: assim toda alteração passa pelas regras de negócio. */
    void renomear(String novoNome) { this.nome = validarNome(novoNome); }

    /** Papel da pessoa na escola (ex.: "Aluno VIP", "Professor"). */
    public abstract String getPapel();

    /** Linha de descrição para listagens; cada subclasse mostra o que lhe interessa. */
    public abstract String descricaoDetalhada();

    @Override
    public String toString() { return nome; }
}
