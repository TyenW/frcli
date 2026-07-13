package br.com.frcli.manager;

import br.com.frcli.model.*;
import java.util.*;

public class EquipmentManager {

    /**
     * Equipa um item em um slot específico ou determina o melhor slot automaticamente.
     * Qualquer item que já estava no slot é retornado para que possa ser devolvido ao inventário.
     */
    public static List<Equipamento> equipar(Personagem p, Equipamento equip, SlotType slotDesejado) {
        List<Equipamento> desequipados = new ArrayList<>();
        if (p == null || equip == null) return desequipados;

        SlotType slotReal = slotDesejado != null ? slotDesejado : determinarSlotAutomatico(p, equip);

        if (slotReal == null) {
            throw new IllegalArgumentException("Slot incompatível para o item: " + equip.getNome());
        }

        // Valida se o slot é compatível com o item
        if (!validarSlotCompativel(equip.getSlotCompativel(), slotReal)) {
            throw new IllegalArgumentException("Item " + equip.getNome() + " (tipo " + equip.getSlotCompativel() + 
                                               ") não pode ser equipado no slot " + slotReal);
        }

        // Regra de Armas de Duas Mãos
        if (equip.getSlotCompativel() == SlotType.DUAS_MAOS || slotReal == SlotType.DUAS_MAOS) {
            // Desequipa ambas as mãos
            Equipamento principal = p.getEquipamentosEquipados().put(SlotType.MAO_PRINCIPAL, equip);
            Equipamento secundaria = p.getEquipamentosEquipados().put(SlotType.MAO_SECUNDARIA, equip); // Aponta para a mesma arma

            if (principal != null) desequipados.add(principal);
            if (secundaria != null && secundaria != principal) desequipados.add(secundaria);
        } 
        // Se equipar algo em uma mão e houver uma arma de duas mãos equipada, ela deve ser desequipada
        else if (slotReal == SlotType.MAO_PRINCIPAL || slotReal == SlotType.MAO_SECUNDARIA) {
            Equipamento principalAtual = p.getEquipamentosEquipados().get(SlotType.MAO_PRINCIPAL);
            if (principalAtual != null && principalAtual.getSlotCompativel() == SlotType.DUAS_MAOS) {
                // Remove a arma de duas mãos de ambos os slots
                p.getEquipamentosEquipados().put(SlotType.MAO_PRINCIPAL, null);
                p.getEquipamentosEquipados().put(SlotType.MAO_SECUNDARIA, null);
                desequipados.add(principalAtual);
            }

            Equipamento antigo = p.getEquipamentosEquipados().put(slotReal, equip);
            if (antigo != null) {
                desequipados.add(antigo);
            }
        } 
        // Acessórios
        else if (slotReal == SlotType.ACESSORIO_1 || slotReal == SlotType.ACESSORIO_2) {
            Equipamento antigo = p.getEquipamentosEquipados().put(slotReal, equip);
            if (antigo != null) {
                desequipados.add(antigo);
            }
        } 
        // Demais slots (CABECA, TRONCO)
        else {
            Equipamento antigo = p.getEquipamentosEquipados().put(slotReal, equip);
            if (antigo != null) {
                desequipados.add(antigo);
            }
        }

        // Recalcula status finais
        StatusManager.recalcularStatus(p);

        // Dispara eventos no barramento
        for (Equipamento des : desequipados) {
            br.com.frcli.event.EventBus.getInstance().publish(new br.com.frcli.event.ItemDesequipadoEvent(p, des, slotReal));
        }
        br.com.frcli.event.EventBus.getInstance().publish(new br.com.frcli.event.ItemEquipadoEvent(p, equip, slotReal));

        return desequipados;
    }

    /**
     * Desequipa o item de um slot específico.
     */
    public static Equipamento desequipar(Personagem p, SlotType slot) {
        if (p == null || slot == null) return null;

        Equipamento removido = p.getEquipamentosEquipados().get(slot);
        if (removido == null) return null;

        // Se for arma de duas mãos, limpa ambos os slots de mãos
        if (removido.getSlotCompativel() == SlotType.DUAS_MAOS) {
            p.getEquipamentosEquipados().put(SlotType.MAO_PRINCIPAL, null);
            p.getEquipamentosEquipados().put(SlotType.MAO_SECUNDARIA, null);
        } else {
            p.getEquipamentosEquipados().put(slot, null);
        }

        // Recalcula status
        StatusManager.recalcularStatus(p);

        // Dispara evento de desequipar
        br.com.frcli.event.EventBus.getInstance().publish(new br.com.frcli.event.ItemDesequipadoEvent(p, removido, slot));

        return removido;
    }

    private static SlotType determinarSlotAutomatico(Personagem p, Equipamento equip) {
        SlotType comp = equip.getSlotCompativel();
        if (comp == SlotType.CABECA) return SlotType.CABECA;
        if (comp == SlotType.TRONCO) return SlotType.TRONCO;
        if (comp == SlotType.DUAS_MAOS) return SlotType.DUAS_MAOS;
        
        if (comp == SlotType.MAO_PRINCIPAL) {
            return SlotType.MAO_PRINCIPAL;
        }
        if (comp == SlotType.MAO_SECUNDARIA) {
            return SlotType.MAO_SECUNDARIA;
        }
        if (comp == SlotType.ACESSORIO) {
            // Escolhe o slot de acessório vazio ou substitui o primeiro
            if (p.getEquipamentosEquipados().get(SlotType.ACESSORIO_1) == null) {
                return SlotType.ACESSORIO_1;
            }
            if (p.getEquipamentosEquipados().get(SlotType.ACESSORIO_2) == null) {
                return SlotType.ACESSORIO_2;
            }
            return SlotType.ACESSORIO_1;
        }
        return null;
    }

    private static boolean validarSlotCompativel(SlotType compatibilidadeItem, SlotType slotDestino) {
        if (compatibilidadeItem == slotDestino) return true;
        if (compatibilidadeItem == SlotType.ACESSORIO && (slotDestino == SlotType.ACESSORIO_1 || slotDestino == SlotType.ACESSORIO_2)) return true;
        if (compatibilidadeItem == SlotType.DUAS_MAOS && (slotDestino == SlotType.MAO_PRINCIPAL || slotDestino == SlotType.MAO_SECUNDARIA || slotDestino == SlotType.DUAS_MAOS)) return true;
        return false;
    }
}
