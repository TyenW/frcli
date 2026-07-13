package br.com.frcli.event;

import br.com.frcli.model.EntidadeRPG;

public class EntidadeDerrotadaEvent extends RpgEvent {
    private final EntidadeRPG entidade;

    public EntidadeDerrotadaEvent(EntidadeRPG entidade) {
        this.entidade = entidade;
    }

    public EntidadeRPG getEntidade() {
        return entidade;
    }
}
