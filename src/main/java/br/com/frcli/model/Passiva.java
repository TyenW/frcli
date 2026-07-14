package br.com.frcli.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Passiva {
    private String nome;
    private String descricao;
    private String tipoMagiaOuArma; // ex: "Cronomacia", "Pyromacia", etc.
    private String condicao; // ex: "quando estou com raiva"
    private String efeito; // ex: "+10 de forca"

    public Passiva() {}

    @JsonCreator
    public Passiva(
            @JsonProperty("nome") String nome,
            @JsonProperty("descricao") String descricao,
            @JsonProperty("tipoMagiaOuArma") String tipoMagiaOuArma,
            @JsonProperty("condicao") String condicao,
            @JsonProperty("efeito") String efeito) {
        this.nome = nome;
        this.descricao = descricao;
        this.tipoMagiaOuArma = tipoMagiaOuArma;
        this.condicao = condicao;
        this.efeito = efeito;
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

    public String getTipoMagiaOuArma() {
        return tipoMagiaOuArma;
    }

    public void setTipoMagiaOuArma(String tipoMagiaOuArma) {
        this.tipoMagiaOuArma = tipoMagiaOuArma;
    }

    public String getCondition() {
        return condicao;
    }

    // Support both getCondition/getCondicao for flexibility and compatibility
    public String getCondicao() {
        return condicao;
    }

    public void setCondicao(String condicao) {
        this.condicao = condicao;
    }

    public String getEfeito() {
        return efeito;
    }

    public void setEfeito(String efeito) {
        this.efeito = efeito;
    }
}
