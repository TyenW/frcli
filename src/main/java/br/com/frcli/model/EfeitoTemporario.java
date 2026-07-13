package br.com.frcli.model;

public class EfeitoTemporario {
    private String nome;
    private String atributo;
    private double modificador;
    private int turnosRestantes;

    public EfeitoTemporario() {}

    public EfeitoTemporario(String nome, String atributo, double modificador, int turnosRestantes) {
        this.nome = nome;
        this.atributo = atributo;
        this.modificador = modificador;
        this.turnosRestantes = turnosRestantes;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getAtributo() {
        return atributo;
    }

    public void setAtributo(String atributo) {
        this.atributo = atributo;
    }

    public double getModificador() {
        return modificador;
    }

    public void setModificador(double modificador) {
        this.modificador = modificador;
    }

    public int getTurnosRestantes() {
        return turnosRestantes;
    }

    public void setTurnosRestantes(int turnosRestantes) {
        this.turnosRestantes = turnosRestantes;
    }

    public void decrementarTurno() {
        if (this.turnosRestantes > 0) {
            this.turnosRestantes--;
        }
    }

    public boolean estaExpirado() {
        return this.turnosRestantes <= 0;
    }
}
