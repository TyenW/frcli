package br.com.frcli.manager;

import br.com.frcli.model.*;
import java.util.*;

public class LootManager {
    public static class LootResult {
        public List<Item> itens = new ArrayList<>();
        public double moedasG = 0.0;
    }

    public static LootResult gerarLoot(Monstro monstro) {
        LootResult result = new LootResult();
        if (monstro == null || monstro.getLootTable() == null) return result;

        Random rand = new Random();
        for (LootEntry entry : monstro.getLootTable().getEntries()) {
            if (rand.nextDouble() <= entry.getProbabilidade()) {
                if (entry.getTipo().equalsIgnoreCase("MOEDA")) {
                    result.moedasG += entry.getValorMoeda();
                } else if (entry.getTipo().equalsIgnoreCase("ITEM")) {
                    Item catalogItem = ItemFactory.obterItemDoCatalogo(entry.getNomeItem());
                    if (catalogItem != null) {
                        result.itens.add(catalogItem);
                      } else {
                          result.itens.add(new ItemConsumivel(entry.getNomeItem(), "Loot dropado", 10.0, "G$", 1));
                      }
                }
            }
        }
        return result;
    }
}
