package br.com.frcli.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AtributoContagem {
    private int positivos;
    private int negativos;

    public AtributoContagem() {
        this.positivos = 0;
        this.negativos = 0;
    }

    @JsonCreator
    public AtributoContagem(
            @JsonProperty("positivos") int positivos,
            @JsonProperty("negativos") int negativos) {
        this.positivos = positivos;
        this.negativos = negativos;
    }

    public int getPositivos() {
        return positivos;
    }

    public void setPositivos(int positivos) {
        this.positivos = positivos;
    }

    public int getNegativos() {
        return negativos;
    }

    public void setNegativos(int negativos) {
        this.negativos = negativos;
    }

    public void incrementarPositivos() {
        this.positivos++;
    }

    public void incrementarNegativos() {
        this.negativos++;
    }
}
