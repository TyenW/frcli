package br.com.frcli.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SedeRede extends Loja {
    private String redeId;   // referência à RedeLoja

    public SedeRede() {}

    @JsonCreator
    public SedeRede(
            @JsonProperty("id") String id,
            @JsonProperty("nome") String nome,
            @JsonProperty("cidade") String cidade,
            @JsonProperty("caixa") double caixa,
            @JsonProperty("redeId") String redeId) {
        super(id, nome, cidade, caixa);
        this.redeId = redeId;
    }

    public String getRedeId() {
        return redeId;
    }

    public void setRedeId(String redeId) {
        this.redeId = redeId;
    }
}
