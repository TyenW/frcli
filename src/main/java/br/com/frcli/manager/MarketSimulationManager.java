package br.com.frcli.manager;

import br.com.frcli.model.*;
import java.util.*;

public class MarketSimulationManager {

    private static final double FATOR_MAX_POR_TRANSACAO = 0.6;
    private static final Random random = new Random();

    public static List<String> atualizarLojas() {
        List<Loja> lojas = LojaManager.listarTodasLojas();
        List<String> logs = new ArrayList<>();
        for (Loja l : lojas) {
            logs.addAll(atualizarLoja(l));
        }
        LojaManager.salvarLojas();
        return logs;
    }

    public static List<String> atualizarLoja(Loja loja) {
        if (loja == null) return Collections.emptyList();

        List<String> logs = new ArrayList<>();
        int poderInicial = loja.getPoder();
        int numEventos = random.nextInt(5) + 1; // 1 a 5 eventos

        List<Item> catalogoGlobal = ItemFactory.listarCatalogo();
        
        for (int i = 0; i < numEventos; i++) {
            boolean compra = random.nextBoolean();

            if (compra) {
                double poderMaxItem = loja.getPoder() * FATOR_MAX_POR_TRANSACAO;
                List<Item> elegiveis = new ArrayList<>();
                for (Item item : catalogoGlobal) {
                    int pVal = item.getPoder() != null ? item.getPoder() : 1;
                    if (pVal <= poderMaxItem) {
                        elegiveis.add(item);
                    }
                }

                if (!elegiveis.isEmpty()) {
                    Item sorteado = elegiveis.get(random.nextInt(elegiveis.size()));
                    double custoRef = sorteado.getValorComercial();
                    if (loja.getCaixa() >= custoRef) {
                        String chave = sorteado.getNome().toLowerCase();
                        loja.getEstoque().put(chave, loja.getEstoque().getOrDefault(chave, 0) + 1);
                        loja.setCaixa(loja.getCaixa() - custoRef);
                        logs.add(String.format("[%s] +1x %s (compra de estoque / G$ -%.2f)", loja.getNome(), sorteado.getNome(), custoRef));
                    }
                }
            } else {
                if (!loja.getEstoque().isEmpty()) {
                    List<String> itensNoEstoque = new ArrayList<>(loja.getEstoque().keySet());
                    String itemSorteadoChave = itensNoEstoque.get(random.nextInt(itensNoEstoque.size()));
                    int qtd = loja.getEstoque().get(itemSorteadoChave);
                    Item item = ItemFactory.obterItemDoCatalogo(itemSorteadoChave);
                    
                    if (item != null) {
                        if (qtd > 1) {
                            loja.getEstoque().put(itemSorteadoChave, qtd - 1);
                        } else {
                            loja.getEstoque().remove(itemSorteadoChave);
                        }

                        double precoVenda = TradeNetworkManager.obterPrecoVendaLoja(loja, item);
                        loja.setCaixa(loja.getCaixa() + precoVenda);
                        logs.add(String.format("[%s] -1x %s (vendido a aventureiro / G$ +%.2f)", loja.getNome(), item.getNome(), precoVenda));
                    }
                }
            }
        }

        loja.setPoder(ShopPowerCalculator.calcularPoder(loja));
        logs.add(String.format("[%s] Poder atualizado: %d ➔ %d (Caixa: G$ %.2f)", loja.getNome(), poderInicial, loja.getPoder(), loja.getCaixa()));
        
        return logs;
    }
}
