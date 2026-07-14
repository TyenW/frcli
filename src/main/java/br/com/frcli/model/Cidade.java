package br.com.frcli.model;

public class Cidade {
    private String nome;
    private String regiao;          // opcional, agrupamento
    private double indiceRiqueza;   // 0.5–2.0, multiplicador econômico local

    public Cidade() {}

    public Cidade(String nome, String regiao, double indiceRiqueza) {
        this.nome = nome;
        this.regiao = regiao;
        this.indiceRiqueza = indiceRiqueza;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getRegiao() {
        return regiao;
    }

    public void setRegiao(String regiao) {
        this.regiao = regiao;
    }

    public double getIndiceRiqueza() {
        return indiceRiqueza;
    }

    public void setIndiceRiqueza(double indiceRiqueza) {
        this.indiceRiqueza = indiceRiqueza;
    }
}
