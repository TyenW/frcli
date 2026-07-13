package br.com.frcli.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemConsumivel extends Item {
    private int quantidadeCargas;

    @JsonCreator
    public ItemConsumivel(
            @JsonProperty("nome") String nome,
            @JsonProperty("descricao") String descricao,
            @JsonProperty("valorComercial") double valorComercial,
            @JsonProperty("tipoMoeda") String tipoMoeda,
            @JsonProperty("quantidadeCargas") int quantidadeCargas) {
        super(nome, descricao, valorComercial, tipoMoeda);
        this.quantidadeCargas = quantidadeCargas;
    }

    public int getQuantidadeCargas() {
        return quantidadeCargas;
    }

    public void setQuantidadeCargas(int quantidadeCargas) {
        this.quantidadeCargas = quantidadeCargas;
    }

    public boolean usar() {
        if (quantidadeCargas > 0) {
            quantidadeCargas--;
            return true;
        }
        return false;
    }
}
