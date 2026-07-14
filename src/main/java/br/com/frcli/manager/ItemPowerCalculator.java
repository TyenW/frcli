package br.com.frcli.manager;

import br.com.frcli.model.Item;
import br.com.frcli.model.Equipamento;
import br.com.frcli.model.ItemConsumivel;

public class ItemPowerCalculator {

    public static int calcularPoder(Item item) {
        if (item == null) return 1;

        double poderBase = 0.0;

        if (item instanceof Equipamento) {
            Equipamento eq = (Equipamento) item;
            
            // Dano
            double danoVal = eq.getDano() != null ? eq.getDano() : 0.0;
            poderBase += danoVal * 1.5;

            // Modificadores de status
            if (eq.getModificadoresStatus() != null) {
                double somaMods = 0.0;
                for (Double val : eq.getModificadoresStatus().values()) {
                    if (val != null) {
                        somaMods += Math.abs(val);
                    }
                }
                poderBase += somaMods * 1.0;
            }

            // Habilidades embutidas
            if (eq.getHabilidadesEmbutidas() != null) {
                poderBase += eq.getHabilidadesEmbutidas().size() * 8;
            }

            // Consumo de munição
            if (eq.getTipoMunicao() != null && !eq.getTipoMunicao().isEmpty() && eq.getQuantidadeMunicao() > 0) {
                poderBase += 3.0;
            }

        } else if (item instanceof ItemConsumivel) {
            ItemConsumivel con = (ItemConsumivel) item;
            // Cargas
            poderBase += con.getQuantidadeCargas() * 6.0;
        }

        double mult = item.getRaridade() != null ? item.getRaridade().getMultiplicador() : 1.0;
        int poderFinal = (int) Math.round(poderBase * mult);

        return Math.max(1, poderFinal);
    }
}
