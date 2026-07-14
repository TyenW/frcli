package br.com.frcli.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class Ataque {
    private String nome;
    private String descricao;
    private String tipoMagiaOuArma; // ex: "Cronomacia", "Armas de curta distância"
    private String dano; // ex: "20d100", "3dforca", "forcaD10"
    private List<RequisitoEvolucao> requisitosEvolucao = new ArrayList<>();
    private List<Ataque> evolucoes = new ArrayList<>();
    private int nivel = 1;

    public Ataque() {}

    @JsonCreator
    public Ataque(
            @JsonProperty("nome") String nome,
            @JsonProperty("descricao") String descricao,
            @JsonProperty("tipoMagiaOuArma") String tipoMagiaOuArma,
            @JsonProperty("dano") String dano,
            @JsonProperty("requisitosEvolucao") List<RequisitoEvolucao> requisitosEvolucao,
            @JsonProperty("evolucoes") List<Ataque> evolucoes,
            @JsonProperty("nivel") Integer nivel) {
        this.nome = nome;
        this.descricao = descricao;
        this.tipoMagiaOuArma = tipoMagiaOuArma;
        this.dano = dano;
        this.requisitosEvolucao = requisitosEvolucao != null ? requisitosEvolucao : new ArrayList<>();
        this.evolucoes = evolucoes != null ? evolucoes : new ArrayList<>();
        this.nivel = nivel != null ? nivel : 1;
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

    public String getDano() {
        return dano;
    }

    public void setDano(String dano) {
        this.dano = dano;
    }

    public List<RequisitoEvolucao> getRequisitosEvolucao() {
        return requisitosEvolucao;
    }

    public void setRequisitosEvolucao(List<RequisitoEvolucao> requisitosEvolucao) {
        this.requisitosEvolucao = requisitosEvolucao != null ? requisitosEvolucao : new ArrayList<>();
    }

    public List<Ataque> getEvolucoes() {
        return evolucoes;
    }

    public void setEvolucoes(List<Ataque> evolucoes) {
        this.evolucoes = evolucoes != null ? evolucoes : new ArrayList<>();
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }
}
