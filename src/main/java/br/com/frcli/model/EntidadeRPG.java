package br.com.frcli.model;

import java.util.HashMap;
import java.util.Map;

public abstract class EntidadeRPG {
    protected String nome;
    protected Map<String, Double> statusFinal = new HashMap<>();

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Map<String, Double> getStatusFinal() {
        return statusFinal;
    }

    public void setStatusFinal(Map<String, Double> statusFinal) {
        this.statusFinal = statusFinal;
    }

    public double getStatusFinalAtributo(String atributo) {
        if (atributo == null || statusFinal == null) return 0.0;
        return statusFinal.getOrDefault(atributo.toLowerCase(), 0.0);
    }
}
