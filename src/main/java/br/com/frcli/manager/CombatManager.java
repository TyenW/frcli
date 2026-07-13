package br.com.frcli.manager;

import br.com.frcli.model.*;
import java.util.*;

public class CombatManager {

    /**
     * Consolida todas as habilidades/ações disponíveis para o personagem.
     * Inclui habilidades de classe (conforme o tier de campanha atual) e
     * habilidades embutidas nos equipamentos equipados.
     */
    public static List<Habilidade> obterAcoesDisponiveis(Personagem p) {
        List<Habilidade> acoes = new ArrayList<>();
        if (p == null) return acoes;

        // 1. Habilidades da Classe por Tier (todos os tiers <= tierCampanha atual)
        if (p.getClasse() != null && p.getClasse().getHabilidadesPorTier() != null) {
            for (Map.Entry<Integer, List<Habilidade>> entry : p.getClasse().getHabilidadesPorTier().entrySet()) {
                if (entry.getKey() <= p.getTierCampanha()) {
                    acoes.addAll(entry.getValue());
                }
            }
        }

        // 2. Habilidades dos itens equipados nos slots
        if (p.getEquipamentosEquipados() != null) {
            for (Equipamento equip : p.getEquipamentosEquipados().values()) {
                if (equip != null && equip.getHabilidadesEmbutidas() != null) {
                    acoes.addAll(equip.getHabilidadesEmbutidas());
                }
            }
        }

        return acoes;
    }

    public static double calcularDano(EntidadeRPG defensor, Magia magiaAtacante, double danoBase) {
        if (defensor == null || magiaAtacante == null) return danoBase;

        String nomeMagiaAtacante = StatusManager.normalize(magiaAtacante.getNome());
        double multiplicadorDano = 1.0;

        if (defensor instanceof Personagem) {
            Personagem pDef = (Personagem) defensor;
            if (pDef.getMagias() != null) {
                for (Magia magiaDefensor : pDef.getMagias()) {
                    if (magiaDefensor.getModificadores() != null && magiaDefensor.getModificadores().getNegativos() != null) {
                        for (String neg : magiaDefensor.getModificadores().getNegativos()) {
                            String negNorm = StatusManager.normalize(neg);
                            if (negNorm.contains("resistencia") || negNorm.contains("resistência")) {
                                if (negNorm.contains(nomeMagiaAtacante)) {
                                    multiplicadorDano *= 1.5;
                                }
                            }
                        }
                    }
                }
            }
        }

        return danoBase * multiplicadorDano;
    }
}
