package br.com.frcli.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Equipamento extends Item {
    private SlotType slotCompativel;
    private Map<String, Double> modificadoresStatus; // e.g., {"Vida": 20.0, "Destreza": -1.0}
    private List<Habilidade> habilidadesEmbutidas;
    private Double dano = 0.0;
    private String tipoMunicao;
    private Integer quantidadeMunicao = 0;

    @JsonCreator
    public Equipamento(
            @JsonProperty("nome") String nome,
            @JsonProperty("descricao") String descricao,
            @JsonProperty("valorComercial") double valorComercial,
            @JsonProperty("tipoMoeda") String tipoMoeda,
            @JsonProperty("slotCompativel") SlotType slotCompativel,
            @JsonProperty("modificadoresStatus") Map<String, Double> modificadoresStatus,
            @JsonProperty("habilidadesEmbutidas") List<Habilidade> habilidadesEmbutidas) {
        super(nome, descricao, valorComercial, tipoMoeda);
        this.slotCompativel = slotCompativel;
        this.modificadoresStatus = modificadoresStatus != null ? modificadoresStatus : new HashMap<>();
        this.habilidadesEmbutidas = habilidadesEmbutidas != null ? habilidadesEmbutidas : new ArrayList<>();
    }

    public SlotType getSlotCompativel() {
        return slotCompativel;
    }

    public void setSlotCompativel(SlotType slotCompativel) {
        this.slotCompativel = slotCompativel;
    }

    public Map<String, Double> getModificadoresStatus() {
        return modificadoresStatus;
    }

    public void setModificadoresStatus(Map<String, Double> modificadoresStatus) {
        this.modificadoresStatus = modificadoresStatus;
    }

    public List<Habilidade> getHabilidadesEmbutidas() {
        return habilidadesEmbutidas;
    }

    public void setHabilidadesEmbutidas(List<Habilidade> habilidadesEmbutidas) {
        this.habilidadesEmbutidas = habilidadesEmbutidas;
    }

    public Double getDano() {
        return dano;
    }

    public void setDano(Double dano) {
        this.dano = dano != null ? dano : 0.0;
    }

    public String getTipoMunicao() {
        return tipoMunicao;
    }

    public void setTipoMunicao(String tipoMunicao) {
        this.tipoMunicao = tipoMunicao;
    }

    public Integer getQuantidadeMunicao() {
        return quantidadeMunicao != null ? quantidadeMunicao : 0;
    }

    public void setQuantidadeMunicao(Integer quantidadeMunicao) {
        this.quantidadeMunicao = quantidadeMunicao != null ? quantidadeMunicao : 0;
    }
}
