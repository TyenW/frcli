package br.com.frcli.manager;

import br.com.frcli.model.*;
import java.util.*;

public class TradeNetworkManager {

    public static double obterPrecoVendaLoja(Loja loja, Item item) {
        if (loja == null || item == null) return 0.0;
        double base = item.getValorComercial();
        if (loja instanceof SedeRede) {
            return base * 1.1; // Markup de 10%
        } else if (loja instanceof LojaIndividual) {
            double margem = ((LojaIndividual) loja).getMargemLucro();
            return base * (1.0 + margem);
        }
        return base;
    }

    public static boolean transferirEntreSedes(SedeRede origem, SedeRede destino, String nomeItem, int qtd) {
        if (origem == null || destino == null || nomeItem == null || qtd <= 0) return false;
        if (!origem.getRedeId().equalsIgnoreCase(destino.getRedeId())) {
            System.err.println("[TradeNetwork] Transferência negada: Lojas de redes diferentes!");
            return false;
        }

        nomeItem = nomeItem.toLowerCase();
        int qtdOrigem = origem.getEstoque().getOrDefault(nomeItem, 0);
        if (qtdOrigem < qtd) {
            System.err.println("[TradeNetwork] Estoque insuficiente na origem!");
            return false;
        }

        Item item = ItemFactory.obterItemDoCatalogo(nomeItem);
        if (item == null) {
            System.err.println("[TradeNetwork] Item não encontrado!");
            return false;
        }

        double custoTotal = item.getValorComercial() * qtd;
        if (destino.getCaixa() < custoTotal) {
            System.err.println("[TradeNetwork] Destino sem fundos suficientes!");
            return false;
        }

        origem.getEstoque().put(nomeItem, qtdOrigem - qtd);
        if (origem.getEstoque().get(nomeItem) <= 0) {
            origem.getEstoque().remove(nomeItem);
        }
        destino.getEstoque().put(nomeItem, destino.getEstoque().getOrDefault(nomeItem, 0) + qtd);

        origem.setCaixa(origem.getCaixa() + custoTotal);
        destino.setCaixa(destino.getCaixa() - custoTotal);

        origem.setPoder(ShopPowerCalculator.calcularPoder(origem));
        destino.setPoder(ShopPowerCalculator.calcularPoder(destino));

        LojaManager.salvarLojas();
        return true;
    }

    public static boolean comprarDeFornecedor(LojaIndividual comprador, Loja fornecedor, String nomeItem, int qtd) {
        if (comprador == null || fornecedor == null || nomeItem == null || qtd <= 0) return false;
        
        nomeItem = nomeItem.toLowerCase();
        int qtdFornecedor = fornecedor.getEstoque().getOrDefault(nomeItem, 0);
        if (qtdFornecedor < qtd) {
            System.err.println("[TradeNetwork] Estoque insuficiente no fornecedor!");
            return false;
        }

        Item item = ItemFactory.obterItemDoCatalogo(nomeItem);
        if (item == null) {
            System.err.println("[TradeNetwork] Item não encontrado!");
            return false;
        }

        double precoUnitario = obterPrecoVendaLoja(fornecedor, item);
        double custoTotal = precoUnitario * qtd;
        if (comprador.getCaixa() < custoTotal) {
            System.err.println("[TradeNetwork] Comprador sem fundos suficientes!");
            return false;
        }

        fornecedor.getEstoque().put(nomeItem, qtdFornecedor - qtd);
        if (fornecedor.getEstoque().get(nomeItem) <= 0) {
            fornecedor.getEstoque().remove(nomeItem);
        }
        comprador.getEstoque().put(nomeItem, comprador.getEstoque().getOrDefault(nomeItem, 0) + qtd);

        fornecedor.setCaixa(fornecedor.getCaixa() + custoTotal);
        comprador.setCaixa(comprador.getCaixa() - custoTotal);

        comprador.setPoder(ShopPowerCalculator.calcularPoder(comprador));
        fornecedor.setPoder(ShopPowerCalculator.calcularPoder(fornecedor));

        LojaManager.salvarLojas();
        return true;
    }

    public static Optional<Loja> encontrarMelhorFornecedor(String nomeItem, List<Loja> todasLojas) {
        if (nomeItem == null || todasLojas == null) return Optional.empty();
        String itemLower = nomeItem.toLowerCase();
        Item item = ItemFactory.obterItemDoCatalogo(itemLower);
        if (item == null) return Optional.empty();

        return todasLojas.stream()
            .filter(l -> l.getEstoque().containsKey(itemLower) && l.getEstoque().get(itemLower) > 0)
            .min(Comparator.comparingDouble(l -> obterPrecoVendaLoja(l, item)));
    }
}
