package br.com.frcli.manager;

import br.com.frcli.model.*;

public class InventoryManager {

    /**
     * Adiciona um item à mochila do personagem.
     * Checa o limite de capacidade máxima (15, 30 ou 60).
     */
    public static void adicionarItem(Mochila mochila, Item item) {
        if (mochila == null || item == null) return;

        if (mochila.getItens().size() >= mochila.getMaxItens()) {
            throw new MochilaCheiaException("Mochila Cheia! Capacidade máxima de " + 
                                            mochila.getMaxItens() + " itens atingida.");
        }

        mochila.getItens().add(item);
    }

    /**
     * Remove um item da mochila.
     */
    public static boolean removerItem(Mochila mochila, Item item) {
        if (mochila == null || item == null) return false;
        return mochila.getItens().remove(item);
    }



    /**
     * Adiciona Moeda G$ respeitando o limite da mochila.
     * @param lancarErroSeExceder Se verdadeiro, lança CarteiraCheiaException. Se falso, limita ao teto e descarta o excedente.
     */
    public static void adicionarG(Mochila mochila, double valor, boolean lancarErroSeExceder) {
        if (mochila == null || valor < 0) return;

        double maxG = mochila.getMaxG();
        double valorAtual = mochila.getDinheiroG();
        double novoG = valorAtual + valor;

        if (novoG > maxG) {
            if (lancarErroSeExceder) {
                throw new CarteiraCheiaException("Carteira Cheia! O limite de G$ " + maxG + 
                                                 " para a mochila " + mochila.getTipo() + " foi ultrapassado.");
            } else {
                mochila.setDinheiroG(maxG); // Limita ao teto e descarta o excedente
            }
        } else {
            mochila.setDinheiroG(novoG);
        }
    }

    /**
     * Remove Moeda G$.
     */
    public static boolean gastarG(Mochila mochila, double valor) {
        if (mochila == null || valor < 0) return false;
        if (mochila.getDinheiroG() >= valor) {
            mochila.setDinheiroG(mochila.getDinheiroG() - valor);
            return true;
        }
        return false;
    }
}
