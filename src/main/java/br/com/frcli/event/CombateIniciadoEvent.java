package br.com.frcli.event;

import br.com.frcli.model.EntidadeRPG;

public class CombateIniciadoEvent extends RpgEvent {
    private final EntidadeRPG atacante;
    private final EntidadeRPG defensor;

    public CombateIniciadoEvent(EntidadeRPG atacante, EntidadeRPG defensor) {
        this.atacante = atacante;
        this.defensor = defensor;
    }

    public EntidadeRPG getAtacante() {
        return atacante;
    }

    public EntidadeRPG getDefensor() {
        return defensor;
    }
}
