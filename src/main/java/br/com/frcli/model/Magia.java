package br.com.frcli.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class Magia {
    private String nome;
    private String nomeTraduzido;
    private ModificadoresMagia modificadores;
    private String observacao;

    @JsonCreator
    public Magia(
            @JsonProperty("nome") String nome,
            @JsonProperty("nome_traduzido") String nomeTraduzido,
            @JsonProperty("modificadores") ModificadoresMagia modificadores,
            @JsonProperty("observacao") String observacao) {
        this.nome = nome;
        this.nomeTraduzido = nomeTraduzido;
        this.modificadores = modificadores != null ? modificadores : new ModificadoresMagia(new ArrayList<>(), new ArrayList<>());
        this.observacao = observacao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNomeTraduzido() {
        return nomeTraduzido;
    }

    public void setNomeTraduzido(String nomeTraduzido) {
        this.nomeTraduzido = nomeTraduzido;
    }

    public ModificadoresMagia getModificadores() {
        return modificadores;
    }

    public void setModificadores(ModificadoresMagia modificadores) {
        this.modificadores = modificadores;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public static class ModificadoresMagia {
        private List<String> positivos;
        private List<String> negativos;

        @JsonCreator
        public ModificadoresMagia(
                @JsonProperty("positivos") List<String> positivos,
                @JsonProperty("negativos") List<String> negativos) {
            this.positivos = positivos != null ? positivos : new ArrayList<>();
            this.negativos = negativos != null ? negativos : new ArrayList<>();
        }

        public List<String> getPositivos() {
            return positivos;
        }

        public void setPositivos(List<String> positivos) {
            this.positivos = positivos;
        }

        public List<String> getNegativos() {
            return negativos;
        }

        public void setNegativos(List<String> negativos) {
            this.negativos = negativos;
        }
    }
}
