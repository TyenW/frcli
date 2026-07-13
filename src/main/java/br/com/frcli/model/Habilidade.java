package br.com.frcli.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Habilidade {
    private String nome;
    private String tipo; // "ATIVA" or "PASSIVA"
    private String descricao;

    @JsonCreator
    public Habilidade(
            @JsonProperty("nome") String nome,
            @JsonProperty("tipo") String tipo,
            @JsonProperty("descricao") String descricao) {
        this.nome = nome;
        this.tipo = tipo;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return String.format("%s (%s): %s", nome, tipo, descricao);
    }
}
