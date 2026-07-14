package br.com.frcli.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.*;

public class Personagem extends EntidadeRPG {
    // Identidade
    private String historia;
    private Integer idade;
    private Double tamanho;

    // Composição
    private Raca raca;
    private List<Raca> subRacas; // Utilizado caso a raça seja "Híbrido" (máximo 2 raças)
    private Classe classe;
    private List<Magia> magias;
    private List<Ataque> ataques;
    private List<Passiva> passivas;
    private int tierCampanha; // Nível/Tier na campanha (ex: 1, 2, 3)
    private String cidade = "Cidade Central";

    // Gerenciadores Acoplados (Mochila e Equipamentos)
    private Mochila inventario;
    private Map<SlotType, Equipamento> equipamentosEquipados; // CABECA, TRONCO, MAO_PRINCIPAL, MAO_SECUNDARIA, ACESSORIO_1, ACESSORIO_2

    // Atributos e Modificadores
    private Map<String, Double> statusBase; // Apenas valores iniciais do personagem (ex: Vida: 120, Força: 0, etc.)
    private Map<String, Double> buffsAtivos; // Buffs/Debuffs adicionais em memória
    private List<EfeitoTemporario> efeitosTemporarios; // Buffs e debuffs temporários com TTL em turnos
    private List<String> vantagensEscritas; // Acumulado das descrições de raça, classe, magias
    private List<String> desvantagensEscritas; // Acumulado das descrições negativas
    private Map<String, AtributoContagem> contagemModificadores; // Contagem de + e - por atributo
    private Map<String, Integer> proficiencias = new HashMap<>();

    public Personagem() {
        super();
        this.magias = new ArrayList<>();
        this.ataques = new ArrayList<>();
        this.passivas = new ArrayList<>();
        this.subRacas = new ArrayList<>();
        this.equipamentosEquipados = new HashMap<>();
        this.statusBase = new HashMap<>();
        this.statusFinal = new HashMap<>();
        this.buffsAtivos = new HashMap<>();
        this.efeitosTemporarios = new ArrayList<>();
        this.vantagensEscritas = new ArrayList<>();
        this.desvantagensEscritas = new ArrayList<>();
        this.contagemModificadores = new HashMap<>();
        this.tierCampanha = 1;
        
        this.proficiencias = new HashMap<>();
        this.proficiencias.put("cronomacia", 0);
        this.proficiencias.put("abstractomacia", 0);
        this.proficiencias.put("necromacia", 0);
        this.proficiencias.put("espectromacia", 0);
        this.proficiencias.put("armas de longa distancia", 0);
        this.proficiencias.put("armas de curta distancia", 0);
        
        // Inicializa slots vazios
        this.equipamentosEquipados.put(SlotType.CABECA, null);
        this.equipamentosEquipados.put(SlotType.TRONCO, null);
        this.equipamentosEquipados.put(SlotType.MAO_PRINCIPAL, null);
        this.equipamentosEquipados.put(SlotType.MAO_SECUNDARIA, null);
        this.equipamentosEquipados.put(SlotType.ACESSORIO, null); // Como podemos ter 2 acessórios, vamos mapear usando ACESSORIO_1 e ACESSORIO_2 na lógica
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getHistoria() {
        return historia;
    }

    public void setHistoria(String historia) {
        this.historia = historia;
    }

    public Integer getIdade() {
        return idade;
    }

    public void setIdade(Integer idade) {
        this.idade = idade;
    }

    public Double getTamanho() {
        return tamanho;
    }

    public void setTamanho(Double tamanho) {
        this.tamanho = tamanho;
    }

    public Raca getRaca() {
        return raca;
    }

    public void setRaca(Raca raca) {
        this.raca = raca;
    }

    public List<Raca> getSubRacas() {
        return subRacas;
    }

    public void setSubRacas(List<Raca> subRacas) {
        this.subRacas = subRacas;
    }

    public Classe getClasse() {
        return classe;
    }

    public void setClasse(Classe classe) {
        this.classe = classe;
    }

    public List<Magia> getMagias() {
        return magias;
    }

    public void setMagias(List<Magia> magias) {
        this.magias = magias;
    }

    public int getTierCampanha() {
        return tierCampanha;
    }

    public void setTierCampanha(int tierCampanha) {
        this.tierCampanha = tierCampanha;
    }

    public Mochila getInventario() {
        return inventario;
    }

    public void setInventario(Mochila inventario) {
        this.inventario = inventario;
    }

    public Map<SlotType, Equipamento> getEquipamentosEquipados() {
        return equipamentosEquipados;
    }

    public void setEquipamentosEquipados(Map<SlotType, Equipamento> equipamentosEquipados) {
        this.equipamentosEquipados = equipamentosEquipados;
    }

    public Map<String, Double> getStatusBase() {
        return statusBase;
    }

    public void setStatusBase(Map<String, Double> statusBase) {
        this.statusBase = statusBase;
    }

    public Map<String, Double> getStatusFinal() {
        return statusFinal;
    }

    public void setStatusFinal(Map<String, Double> statusFinal) {
        this.statusFinal = statusFinal;
    }

    public Map<String, Double> getBuffsAtivos() {
        return buffsAtivos;
    }

    public void setBuffsAtivos(Map<String, Double> buffsAtivos) {
        this.buffsAtivos = buffsAtivos;
    }

    public List<EfeitoTemporario> getEfeitosTemporarios() {
        if (efeitosTemporarios == null) {
            efeitosTemporarios = new ArrayList<>();
        }
        return efeitosTemporarios;
    }

    public void setEfeitosTemporarios(List<EfeitoTemporario> efeitosTemporarios) {
        this.efeitosTemporarios = efeitosTemporarios != null ? efeitosTemporarios : new ArrayList<>();
    }

    public List<String> getVantagensEscritas() {
        return vantagensEscritas;
    }

    public void setVantagensEscritas(List<String> vantagensEscritas) {
        this.vantagensEscritas = vantagensEscritas;
    }

    public List<String> getDesvantagensEscritas() {
        return desvantagensEscritas;
    }

    public void setDesvantagensEscritas(List<String> desvantagensEscritas) {
        this.desvantagensEscritas = desvantagensEscritas;
    }

    public Map<String, AtributoContagem> getContagemModificadores() {
        return contagemModificadores;
    }

    public void setContagemModificadores(Map<String, AtributoContagem> contagemModificadores) {
        this.contagemModificadores = contagemModificadores;
    }

    @JsonIgnore
    public double getStatusFinalAtributo(String atributo) {
        return statusFinal.getOrDefault(atributo.toLowerCase(), 0.0);
    }

    public Map<String, Integer> getProficiencias() {
        if (proficiencias == null) {
            proficiencias = new HashMap<>();
        }
        proficiencias.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue() <= 0);
        return proficiencias;
    }

    public void setProficiencias(Map<String, Integer> proficiencias) {
        this.proficiencias = proficiencias != null ? proficiencias : new HashMap<>();
    }

    public List<Ataque> getAtaques() {
        if (ataques == null) {
            ataques = new ArrayList<>();
        }
        return ataques;
    }

    public void setAtaques(List<Ataque> ataques) {
        this.ataques = ataques != null ? ataques : new ArrayList<>();
    }

    public List<Passiva> getPassivas() {
        if (passivas == null) {
            passivas = new ArrayList<>();
        }
        return passivas;
    }

    public void setPassivas(List<Passiva> passivas) {
        this.passivas = passivas != null ? passivas : new ArrayList<>();
    }

    public String getCidade() {
        if (cidade == null || cidade.trim().isEmpty()) {
            cidade = "Cidade Central";
        }
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade != null ? cidade : "Cidade Central";
    }
}
