package br.com.frcli.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "tipoItem"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Equipamento.class, name = "EQUIPAMENTO"),
    @JsonSubTypes.Type(value = ItemConsumivel.class, name = "CONSUMIVEL")
})
public abstract class Item {
    protected String nome;
    protected String descricao;
    protected double valorComercial;
    protected String tipoMoeda; // "G$"

    protected Item() {}

    protected Item(String nome, String descricao, double valorComercial, String tipoMoeda) {
        this.nome = nome;
        this.descricao = descricao;
        this.valorComercial = valorComercial;
        this.tipoMoeda = tipoMoeda;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getValorComercial() {
        return valorComercial;
    }

    public void setValorComercial(double valorComercial) {
        this.valorComercial = valorComercial;
    }

    public String getTipoMoeda() {
        return tipoMoeda;
    }

    public void setTipoMoeda(String tipoMoeda) {
        this.tipoMoeda = tipoMoeda;
    }
}
