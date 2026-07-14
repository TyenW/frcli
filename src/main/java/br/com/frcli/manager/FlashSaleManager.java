package br.com.frcli.manager;

import br.com.frcli.model.Loja;
import java.util.*;

public class FlashSaleManager {

    private static final Map<String, Map<String, Double>> descontosAtivos = new HashMap<>();
    private static final Random random = new Random();

    public static void rolarDescontos(Loja loja) {
        if (loja == null) return;

        Map<String, Double> descontosLoja = new HashMap<>();
        
        double chanceBase = 0.15;
        double chanceAdicional = loja.getPoder() / 1000.0;
        double chanceTotal = Math.min(0.45, chanceBase + chanceAdicional);

        for (String itemChave : loja.getEstoque().keySet()) {
            if (random.nextDouble() < chanceTotal) {
                double limiteMaxDesc = 0.10 + (loja.getPoder() / 2000.0);
                double maxDesc = Math.min(0.50, limiteMaxDesc);
                double minDesc = 0.05;

                double pctDesconto = minDesc + (maxDesc - minDesc) * random.nextDouble();
                pctDesconto = Math.round(pctDesconto * 100.0) / 100.0;

                descontosLoja.put(itemChave.toLowerCase(), pctDesconto);
            }
        }

        descontosAtivos.put(loja.getId().toLowerCase(), descontosLoja);
    }

    public static double obterDescontoRelampago(String lojaId, String nomeItem) {
        if (lojaId == null || nomeItem == null) return 0.0;
        Map<String, Double> descontosLoja = descontosAtivos.get(lojaId.toLowerCase());
        if (descontosLoja == null) return 0.0;
        return descontosLoja.getOrDefault(nomeItem.toLowerCase(), 0.0);
    }

    public static void limparTodosDescontos() {
        descontosAtivos.clear();
    }
}
