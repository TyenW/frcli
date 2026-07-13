package br.com.frcli.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Classe {
    private String classe; // Class name
    private String especialidades;
    private BalanceamentoStatus balanceamentoDeStatus;
    private String tamanhoDaMochila; // e.g. "pequena", "média", "grande"
    private Map<Integer, List<Habilidade>> habilidadesPorTier; // Tier -> list of abilities unlocked

    @JsonCreator
    public Classe(
            @JsonProperty("classe") String classe,
            @JsonProperty("especialidades") String especialidades,
            @JsonProperty("balanceamento_de_status") BalanceamentoStatus balanceamentoDeStatus,
            @JsonProperty("tamanho_da_mochila") String tamanhoDaMochila,
            @JsonProperty("habilidadesPorTier") Map<Integer, List<Habilidade>> habilidadesPorTier) {
        this.classe = classe;
        this.especialidades = especialidades;
        this.balanceamentoDeStatus = balanceamentoDeStatus != null ? balanceamentoDeStatus : new BalanceamentoStatus(new ArrayList<>(), new ArrayList<>());
        this.tamanhoDaMochila = tamanhoDaMochila;
        this.habilidadesPorTier = habilidadesPorTier != null ? habilidadesPorTier : new HashMap<>();
    }

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }

    public String getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(String especialidades) {
        this.especialidades = especialidades;
    }

    public BalanceamentoStatus getBalanceamentoDeStatus() {
        return balanceamentoDeStatus;
    }

    public void setBalanceamentoDeStatus(BalanceamentoStatus balanceamentoDeStatus) {
        this.balanceamentoDeStatus = balanceamentoDeStatus;
    }

    public String getTamanhoDaMochila() {
        return tamanhoDaMochila;
    }

    public void setTamanhoDaMochila(String tamanhoDaMochila) {
        this.tamanhoDaMochila = tamanhoDaMochila;
    }

    public Map<Integer, List<Habilidade>> getHabilidadesPorTier() {
        return habilidadesPorTier;
    }

    public void setHabilidadesPorTier(Map<Integer, List<Habilidade>> habilidadesPorTier) {
        this.habilidadesPorTier = habilidadesPorTier;
    }

    public void adicionarHabilidadeAoTier(int tier, Habilidade habilidade) {
        this.habilidadesPorTier.computeIfAbsent(tier, k -> new ArrayList<>()).add(habilidade);
    }

    public static class BalanceamentoStatus {
        private List<String> positivos;
        private List<String> negativos;

        @JsonCreator
        public BalanceamentoStatus(
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
