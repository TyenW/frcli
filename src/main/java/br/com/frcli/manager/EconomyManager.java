package br.com.frcli.manager;

import br.com.frcli.model.*;

public class EconomyManager {

    public static double calcularPrecoCompra(Item item, Personagem p) {
        if (item == null || p == null) return 0.0;
        double carisma = p.getStatusFinalAtributo("carisma");
        double desconto = Math.min(0.50, Math.max(0.0, carisma / 50.0)); // Desconto máx 50%
        return item.getValorComercial() * (1.0 - desconto);
    }

    public static double calcularPrecoVenda(Item item, Personagem p) {
        if (item == null || p == null) return 0.0;
        return item.getValorComercial() * 0.50;
    }

    public static boolean comprarItem(Personagem p, Item item) {
        if (p == null || item == null) return false;
        Mochila mochila = p.getInventario();

        if (mochila.getItens().size() >= mochila.getMaxItens()) {
            return false;
        }

        double preco = calcularPrecoCompra(item, p);

        if (InventoryManager.gastarG(mochila, preco)) {
            try {
                InventoryManager.adicionarItem(mochila, ItemFactory.clonarItem(item));
                return true;
            } catch (Exception e) {
                InventoryManager.adicionarG(mochila, preco, false);
                System.err.println("[EconomyManager] Falha ao adicionar item, dinheiro devolvido.");
            }
        }
        return false;
    }

    public static boolean venderItem(Personagem p, Item item) {
        if (p == null || item == null) return false;
        Mochila mochila = p.getInventario();

        if (InventoryManager.removerItem(mochila, item)) {
            double valorVenda = calcularPrecoVenda(item, p);
            try {
                InventoryManager.adicionarG(mochila, valorVenda, false);
                return true;
            } catch (Exception e) {
                mochila.getItens().add(item);
            }
        }
        return false;
    }
}
