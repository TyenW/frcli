package br.com.frcli.manager;

import br.com.frcli.model.Item;

public class PricingEngine {

    public static double precoBaseSugerido(Item item) {
        if (item == null) return 1.0;
        
        // Se o poder não foi calculado ainda, calcula temporariamente para precificar
        int poder = item.getPoder() != null ? item.getPoder() : ItemPowerCalculator.calcularPoder(item);
        
        double precoPorPoder = 4.5; // G$ por ponto de poder
        double preco = poder * precoPorPoder;
        
        return Math.max(1.0, Math.round(preco * 100.0) / 100.0);
    }
}
