package br.com.frcli.manager;

import br.com.frcli.model.*;
import java.util.Map;

public class ShopPowerCalculator {

    public static int calcularPoder(Loja loja) {
        if (loja == null) return 1;

        double somaPoderEstoque = 0.0;
        if (loja.getEstoque() != null) {
            for (Map.Entry<String, Integer> entry : loja.getEstoque().entrySet()) {
                Item item = ItemFactory.obterItemDoCatalogo(entry.getKey());
                if (item != null && entry.getValue() != null) {
                    int pVal = item.getPoder() != null ? item.getPoder() : 1;
                    somaPoderEstoque += pVal * entry.getValue();
                }
            }
        }

        double valorCidade = 0.0;
        Cidade city = CidadeManager.obter(loja.getCidade());
        if (city != null) {
            valorCidade = city.getIndiceRiqueza() * 20.0;
        } else {
            valorCidade = 1.0 * 20.0;
        }

        double valorRede = 0.0;
        if (loja instanceof SedeRede) {
            SedeRede sr = (SedeRede) loja;
            RedeLoja rl = LojaManager.obterRede(sr.getRedeId());
            if (rl != null) {
                valorRede = rl.getSedeIds().size() * 10.0;
            }
        }

        double poder = (somaPoderEstoque * 0.4) + (loja.getCaixa() / 50.0) + valorCidade + valorRede;
        return Math.max(1, (int) Math.round(poder));
    }
}
