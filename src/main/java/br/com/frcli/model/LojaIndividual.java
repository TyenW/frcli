package br.com.frcli.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LojaIndividual extends Loja {
    private double margemLucro;  // % de markup sobre preço de compra, ex. 0.35 = revende 35% mais caro
    private String dono;         // nome do NPC comerciante (flavor, opcional)

    public LojaIndividual() {}

    @JsonCreator
    public LojaIndividual(
            @JsonProperty("id") String id,
            @JsonProperty("nome") String nome,
            @JsonProperty("cidade") String cidade,
            @JsonProperty("caixa") double caixa,
            @JsonProperty("margemLucro") double margemLucro,
            @JsonProperty("dono") String dono) {
        super(id, nome, cidade, caixa);
        this.margemLucro = margemLucro;
        this.dono = dono;
    }

    public double getMargemLucro() {
        return margemLucro;
    }

    public void setMargemLucro(double margemLucro) {
        this.margemLucro = margemLucro;
    }

    public String getDono() {
        return dono;
    }

    public void setDono(String dono) {
        this.dono = dono;
    }
}
