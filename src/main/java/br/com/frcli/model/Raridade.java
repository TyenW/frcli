package br.com.frcli.model;

public enum Raridade {
    COMUM(1.0),
    INCOMUM(1.5),
    RARO(2.5),
    EPICO(4.0),
    LENDARIO(7.0);

    private final double multiplicador;

    Raridade(double multiplicador) {
        this.multiplicador = multiplicador;
    }

    public double getMultiplicador() {
        return multiplicador;
    }
}
