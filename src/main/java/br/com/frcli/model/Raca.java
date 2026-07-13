package br.com.frcli.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class Raca {
    private String raca; // Race name
    private ModificadoresInfo vantagens;
    private ModificadoresInfo desvantagens;

    @JsonCreator
    public Raca(
            @JsonProperty("raca") String raca,
            @JsonProperty("vantagens") ModificadoresInfo vantagens,
            @JsonProperty("desvantagens") ModificadoresInfo desvantagens) {
        this.raca = raca;
        this.vantagens = vantagens != null ? vantagens : new ModificadoresInfo(new ArrayList<>(), new ArrayList<>());
        this.desvantagens = desvantagens != null ? desvantagens : new ModificadoresInfo(new ArrayList<>(), new ArrayList<>());
    }

    public String getRaca() {
        return raca;
    }

    public void setRaca(String raca) {
        this.raca = raca;
    }

    public ModificadoresInfo getVantagens() {
        return vantagens;
    }

    public void setVantagens(ModificadoresInfo vantagens) {
        this.vantagens = vantagens;
    }

    public ModificadoresInfo getDesvantagens() {
        return desvantagens;
    }

    public void setDesvantagens(ModificadoresInfo desvantagens) {
        this.desvantagens = desvantagens;
    }

    public static class ModificadoresInfo {
        private List<String> modificadores;
        private List<String> escritas;

        @JsonCreator
        public ModificadoresInfo(
                @JsonProperty("modificadores") List<String> modificadores,
                @JsonProperty("escritas") List<String> escritas) {
            this.modificadores = modificadores != null ? modificadores : new ArrayList<>();
            this.escritas = escritas != null ? escritas : new ArrayList<>();
        }

        public List<String> getModificadores() {
            return modificadores;
        }

        public void setModificadores(List<String> modificadores) {
            this.modificadores = modificadores;
        }

        public List<String> getEscritas() {
            return escritas;
        }

        public void setEscritas(List<String> escritas) {
            this.escritas = escritas;
        }
    }
}
