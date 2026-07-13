package br.com.frcli.model;

public class LootEntry {
    private double probabilidade; // ex: 0.70 para 70%
    private String tipo; // "MOEDA" ou "ITEM"
    private String nomeItem; // Se for ITEM
    private double valorMoeda; // Se for MOEDA

    public LootEntry() {}

    public LootEntry(double probabilidade, String tipo, String nomeItem, double valorMoeda) {
        this.probabilidade = probabilidade;
        this.tipo = tipo;
        this.nomeItem = nomeItem;
        this.valorMoeda = valorMoeda;
    }

    public double getProbabilidade() {
        return probabilidade;
    }

    public void setProbabilidade(double probabilidade) {
        this.probabilidade = probabilidade;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNomeItem() {
        return nomeItem;
    }

    public void setNomeItem(String nomeItem) {
        this.nomeItem = nomeItem;
    }

    public double getValorMoeda() {
        return valorMoeda;
    }

    public void setValorMoeda(double valorMoeda) {
        this.valorMoeda = valorMoeda;
    }
}
