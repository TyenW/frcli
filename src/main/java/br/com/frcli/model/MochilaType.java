package br.com.frcli.model;

public enum MochilaType {
    PEQUENA(15, 25000.0),
    MEDIA(30, 70000.0),
    GRANDE(60, 150999.99);

    private final int maxItens;
    private final double maxG;

    MochilaType(int maxItens, double maxG) {
        this.maxItens = maxItens;
        this.maxG = maxG;
    }

    public int getMaxItens() {
        return maxItens;
    }

    public double getMaxG() {
        return maxG;
    }
}
