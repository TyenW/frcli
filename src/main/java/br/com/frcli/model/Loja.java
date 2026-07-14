package br.com.frcli.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.HashMap;
import java.util.Map;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "tipoLoja"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = SedeRede.class, name = "SEDE_REDE"),
    @JsonSubTypes.Type(value = LojaIndividual.class, name = "INDIVIDUAL")
})
public abstract class Loja {
    protected String id;              // slug/id único
    protected String nome;
    protected String cidade;          // nome da Cidade
    protected Map<String, Integer> estoque = new HashMap<>();    // nome item (lowercase) -> quantidade
    protected double caixa;           // caixa disponível em G$
    protected int poder;              // calculado automaticamente

    protected Loja() {}

    protected Loja(String id, String nome, String cidade, double caixa) {
        this.id = id;
        this.nome = nome;
        this.cidade = cidade;
        this.caixa = caixa;
        this.estoque = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public Map<String, Integer> getEstoque() {
        if (estoque == null) estoque = new HashMap<>();
        return estoque;
    }

    public void setEstoque(Map<String, Integer> estoque) {
        this.estoque = estoque;
    }

    public double getCaixa() {
        return caixa;
    }

    public void setCaixa(double caixa) {
        this.caixa = caixa;
    }

    public int getPoder() {
        return poder;
    }

    public void setPoder(int poder) {
        this.poder = poder;
    }
}
