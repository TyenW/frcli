package br.com.frcli.manager;

import br.com.frcli.model.*;
import br.com.frcli.repository.FichaRepository;
import java.util.Map;

public class LojaEconomyManager {

    public static double calcularPrecoVendaAoJogador(Loja loja, Item item, Personagem p) {
        if (loja == null || item == null || p == null) return 0.0;

        // 1. Preço de tabela com markup da loja
        double precoBaseLoja = TradeNetworkManager.obterPrecoVendaLoja(loja, item);

        // 2. Desconto Relâmpago (Flash Sale) em memória
        double descontoRelampago = FlashSaleManager.obterDescontoRelampago(loja.getId(), item.getNome());
        double precoComDesconto = precoBaseLoja * (1.0 - descontoRelampago);

        // 3. Desconto por Carisma
        double carisma = p.getStatusFinalAtributo("carisma");
        double descontoCarisma = Math.min(0.50, Math.max(0.0, carisma / 50.0));
        double precoFinal = precoComDesconto * (1.0 - descontoCarisma);

        // 4. Custo de frete intermunicipal (G$ 15.0 se a loja estiver em cidade diferente da do personagem)
        if (!loja.getCidade().equalsIgnoreCase(p.getCidade())) {
            precoFinal += 15.0;
        }

        return Math.max(1.0, Math.round(precoFinal * 100.0) / 100.0);
    }

    public static double calcularPrecoCompraDoJogador(Loja loja, Item item, Personagem p) {
        if (loja == null || item == null || p == null) return 0.0;
        // Lojas compram itens do jogador por 50% do valor comercial base
        double preco = item.getValorComercial() * 0.50;
        return Math.max(1.0, Math.round(preco * 100.0) / 100.0);
    }

    public static boolean comprarDaLoja(Personagem p, Loja loja, String nomeItem) {
        if (p == null || loja == null || nomeItem == null) return false;
        nomeItem = nomeItem.toLowerCase();

        int qtdEstoque = loja.getEstoque().getOrDefault(nomeItem, 0);
        if (qtdEstoque <= 0) {
            System.out.println("❌ O estabelecimento não possui este item em estoque!");
            return false;
        }

        Item item = ItemFactory.obterItemDoCatalogo(nomeItem);
        if (item == null) {
            System.out.println("❌ Item inválido ou não cadastrado no catálogo!");
            return false;
        }

        Mochila mochila = p.getInventario();
        if (mochila.getItens().size() >= mochila.getMaxItens()) {
            System.out.println("❌ Mochila cheia!");
            return false;
        }

        double precoFinal = calcularPrecoVendaAoJogador(loja, item, p);
        if (mochila.getDinheiroG() < precoFinal) {
            System.out.printf("❌ Dinheiro insuficiente! Custo com frete/desconto: G$ %.2f\n", precoFinal);
            return false;
        }

        // Executa a transação
        if (InventoryManager.gastarG(mochila, precoFinal)) {
            try {
                // Adiciona na mochila
                InventoryManager.adicionarItem(mochila, ItemFactory.clonarItem(item));

                // Atualiza o caixa da loja
                loja.setCaixa(loja.getCaixa() + precoFinal);

                // Deduz do estoque da loja
                loja.getEstoque().put(nomeItem, qtdEstoque - 1);
                if (loja.getEstoque().get(nomeItem) <= 0) {
                    loja.getEstoque().remove(nomeItem);
                }

                // Recalcula poder da loja
                loja.setPoder(ShopPowerCalculator.calcularPoder(loja));

                // Salva estados
                LojaManager.salvarLojas();
                FichaRepository repo = new FichaRepository();
                repo.salvar(p);

                return true;
            } catch (Exception e) {
                // Rolo back de fundos
                InventoryManager.adicionarG(mochila, precoFinal, false);
                System.err.println("❌ Erro inesperado ao realizar compra. O ouro foi estornado.");
            }
        }

        return false;
    }

    public static boolean venderParaLoja(Personagem p, Loja loja, Item item) {
        if (p == null || loja == null || item == null) return false;
        
        double precoCompra = calcularPrecoCompraDoJogador(loja, item, p);
        if (loja.getCaixa() < precoCompra) {
            System.out.printf("❌ A loja não possui capital suficiente para comprar o item! (Caixa da loja: G$ %.2f)\n", loja.getCaixa());
            return false;
        }

        Mochila mochila = p.getInventario();
        if (InventoryManager.removerItem(mochila, item)) {
            try {
                // Adiciona ouro ao jogador
                InventoryManager.adicionarG(mochila, precoCompra, false);

                // Deduz do caixa da loja
                loja.setCaixa(loja.getCaixa() - precoCompra);

                // Adiciona ao estoque da loja
                String nomeItem = item.getNome().toLowerCase();
                loja.getEstoque().put(nomeItem, loja.getEstoque().getOrDefault(nomeItem, 0) + 1);

                // Recalcula poder
                loja.setPoder(ShopPowerCalculator.calcularPoder(loja));

                // Salva
                LojaManager.salvarLojas();
                FichaRepository repo = new FichaRepository();
                repo.salvar(p);

                return true;
            } catch (Exception e) {
                // Rollback
                mochila.getItens().add(item);
                System.err.println("❌ Erro ao concluir venda. O item retornou à mochila.");
            }
        }

        return false;
    }
}
