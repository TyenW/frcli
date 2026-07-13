package br.com.frcli.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Mochila {
    private MochilaType tipo;
    private List<Item> itens;
    private double dinheiroG;

    @JsonCreator
    public Mochila(
            @JsonProperty("tipo") MochilaType tipo,
            @JsonProperty("itens") List<Item> itens,
            @JsonProperty("dinheiroG") double dinheiroG) {
        this.tipo = tipo;
        this.itens = itens != null ? itens : new ArrayList<>();
        this.dinheiroG = dinheiroG;
    }

    public MochilaType getTipo() {
        return tipo;
    }

    public void setTipo(MochilaType tipo) {
        this.tipo = tipo;
    }

    public List<Item> getItens() {
        return itens;
    }

    public void setItens(List<Item> itens) {
        this.itens = itens;
    }

    public double getDinheiroG() {
        return dinheiroG;
    }

    public void setDinheiroG(double dinheiroG) {
        this.dinheiroG = dinheiroG;
    }

    @JsonIgnore
    public int getMaxItens() {
        return tipo != null ? tipo.getMaxItens() : Integer.MAX_VALUE;
    }

    @JsonIgnore
    public double getMaxG() {
        return tipo != null ? tipo.getMaxG() : Double.MAX_VALUE;
    }
}
