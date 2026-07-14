package br.com.frcli.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RequisitoEvolucao {
    private String tipo; // ex: "PROFICIENCIA", "ATAQUE_PREVIO", "TIER"
    private String nomeRequisito; // ex: "Cronomacia", "Soco"
    private int valor; // ex: 5, 1

    public RequisitoEvolucao() {}

    @JsonCreator
    public RequisitoEvolucao(
            @JsonProperty("tipo") String tipo,
            @JsonProperty("nomeRequisito") String nomeRequisito,
            @JsonProperty("valor") int valor) {
        this.tipo = tipo;
        this.nomeRequisito = nomeRequisito;
        this.valor = valor;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNomeRequisito() {
        return nomeRequisito;
    }

    public void setNomeRequisito(String nomeRequisito) {
        this.nomeRequisito = nomeRequisito;
    }

    public int getValor() {
        return valor;
    }

    public void setValor(int valor) {
        this.valor = valor;
    }

    @Override
    public String toString() {
        if ("PROFICIENCIA".equalsIgnoreCase(tipo)) {
            return "Requer proficiência " + valor + " em " + nomeRequisito;
        } else if ("ATAQUE_PREVIO".equalsIgnoreCase(tipo)) {
            return "Requer ataque prévio: " + nomeRequisito;
        } else if ("TIER".equalsIgnoreCase(tipo)) {
            return "Requer Tier " + valor;
        }
        return tipo + ": " + nomeRequisito + " (" + valor + ")";
    }
}
