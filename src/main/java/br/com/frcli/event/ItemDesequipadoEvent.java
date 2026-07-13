package br.com.frcli.event;

import br.com.frcli.model.Personagem;
import br.com.frcli.model.Equipamento;
import br.com.frcli.model.SlotType;

public class ItemDesequipadoEvent extends RpgEvent {
    private final Personagem personagem;
    private final Equipamento equipamento;
    private final SlotType slot;

    public ItemDesequipadoEvent(Personagem personagem, Equipamento equipamento, SlotType slot) {
        this.personagem = personagem;
        this.equipamento = equipamento;
        this.slot = slot;
    }

    public Personagem getPersonagem() {
        return personagem;
    }

    public Equipamento getEquipamento() {
        return equipamento;
    }

    public SlotType getSlot() {
        return slot;
    }
}
