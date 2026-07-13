package br.com.frcli.model;

import java.util.ArrayList;
import java.util.List;

public class LootTable {
    private List<LootEntry> entries = new ArrayList<>();

    public LootTable() {}

    public LootTable(List<LootEntry> entries) {
        this.entries = entries != null ? entries : new ArrayList<>();
    }

    public List<LootEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<LootEntry> entries) {
        this.entries = entries;
    }
}
