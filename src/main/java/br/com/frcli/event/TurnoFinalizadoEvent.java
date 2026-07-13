package br.com.frcli.event;

import br.com.frcli.model.Personagem;

public class TurnoFinalizadoEvent extends RpgEvent {
    private final Personagem personagem;
    private final int numeroTurno;

    public TurnoFinalizadoEvent(Personagem personagem, int numeroTurno) {
        this.personagem = personagem;
        this.numeroTurno = numeroTurno;
    }

    public Personagem getPersonagem() {
        return personagem;
    }

    public int getNumeroTurno() {
        return numeroTurno;
    }
}
