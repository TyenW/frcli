package br.com.frcli.model;

import java.util.ArrayList;
import java.util.List;

public class Monstro extends EntidadeRPG {
    private String tipo;
    private List<String> ataquesFixos = new ArrayList<>();
    private LootTable lootTable;

    public Monstro() {
        super();
        this.nome = "Monstro";
    }

    public Monstro(String nome, String tipo, List<String> ataquesFixos, LootTable lootTable) {
        super();
        this.nome = nome;
        this.tipo = tipo;
        this.ataquesFixos = ataquesFixos != null ? ataquesFixos : new ArrayList<>();
        this.lootTable = lootTable;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public List<String> getAtaquesFixos() {
        return ataquesFixos;
    }

    public void setAtaquesFixos(List<String> ataquesFixos) {
        this.ataquesFixos = ataquesFixos;
    }

    public LootTable getLootTable() {
        return lootTable;
    }

    public void setLootTable(LootTable lootTable) {
        this.lootTable = lootTable;
    }
}
