package br.com.frcli.manager;

import br.com.frcli.model.*;
import java.util.*;

public class StatusManager {

    public static class AtributoModifier {
        private final double positivo;
        private final double negativo;

        public AtributoModifier(double positivo, double negativo) {
            this.positivo = positivo;
            this.negativo = negativo;
        }

        public double getPositivo() {
            return positivo;
        }

        public double getNegativo() {
            return negativo;
        }
    }

    private static final Map<String, AtributoModifier> MODIFICADORES_PADRAO = new HashMap<>();

    static {
        // Atributos padrão do status.json
        MODIFICADORES_PADRAO.put("vida", new AtributoModifier(75.0, -35.0));
        MODIFICADORES_PADRAO.put("forca", new AtributoModifier(3.0, -0.5));
        MODIFICADORES_PADRAO.put("velocidade", new AtributoModifier(3.0, -0.5));
        MODIFICADRAP_PADRAO_PUT_INTELIGENCIA();
        MODIFICADORES_PADRAO.put("destreza", new AtributoModifier(2.0, -1.0));
        MODIFICADORES_PADRAO.put("intelectualidade", new AtributoModifier(4.0, -1.5));
        MODIFICADORES_PADRAO.put("carisma", new AtributoModifier(4.0, -1.5));

        // Atributos extras referenciados nas regras
        MODIFICADORES_PADRAO.put("cura", new AtributoModifier(10.0, -5.0));
        MODIFICADORES_PADRAO.put("confianca", new AtributoModifier(2.0, -1.0));
        MODIFICADORES_PADRAO.put("sanidade", new AtributoModifier(10.0, -5.0));
        MODIFICADORES_PADRAO.put("ataque", new AtributoModifier(5.0, -2.0));
    }

    private static void MODIFICADRAP_PADRAO_PUT_INTELIGENCIA() {
        MODIFICADORES_PADRAO.put("inteligencia", new AtributoModifier(2.0, -1.0));
        MODIFICADORES_PADRAO.put("inteligência", new AtributoModifier(2.0, -1.0));
    }

    public static String normalize(String str) {
        if (str == null) return "";
        str = str.trim().toLowerCase();
        str = str.replace("ç", "c")
                 .replace("á", "a")
                 .replace("â", "a")
                 .replace("ã", "a")
                 .replace("é", "e")
                 .replace("ê", "e")
                 .replace("í", "i")
                 .replace("ó", "o")
                 .replace("ô", "o")
                 .replace("õ", "o")
                 .replace("ú", "u");
        if (str.equals("defesa")) {
            return "vida";
        }
        return str;
    }

    public static AtributoModifier getModifier(String atributo) {
        String key = normalize(atributo);
        // Retorna o modificador padrão ou um fallback dinâmico (positivo=1.0, negativo=-1.0)
        return MODIFICADORES_PADRAO.getOrDefault(key, new AtributoModifier(1.0, -1.0));
    }

    public static void recalcularStatus(Personagem p) {
        if (p == null) return;

        // Limpa as vantagens/desvantagens escritas e contagens de modificadores
        p.getVantagensEscritas().clear();
        p.getDesvantagensEscritas().clear();
        p.getContagemModificadores().clear();

        // 1. Popula vantagens e desvantagens escritas da raça (combina se for híbrido)
        if (p.getRaca() != null) {
            String nomeRaca = normalize(p.getRaca().getRaca());
            if (nomeRaca.equals("hibrido") || nomeRaca.equals("híbrido")) {
                for (Raca sub : p.getSubRacas()) {
                    if (sub.getVantagens() != null && sub.getVantagens().getEscritas() != null) {
                        p.getVantagensEscritas().addAll(sub.getVantagens().getEscritas());
                    }
                    if (sub.getDesvantagens() != null && sub.getDesvantagens().getEscritas() != null) {
                        p.getDesvantagensEscritas().addAll(sub.getDesvantagens().getEscritas());
                    }
                }
            } else {
                if (p.getRaca().getVantagens() != null && p.getRaca().getVantagens().getEscritas() != null) {
                    p.getVantagensEscritas().addAll(p.getRaca().getVantagens().getEscritas());
                }
                if (p.getRaca().getDesvantagens() != null && p.getRaca().getDesvantagens().getEscritas() != null) {
                    p.getDesvantagensEscritas().addAll(p.getRaca().getDesvantagens().getEscritas());
                }
            }
        }

        // 2. Popula especialidades da classe
        if (p.getClasse() != null) {
            if (p.getClasse().getEspecialidades() != null && !p.getClasse().getEspecialidades().isEmpty()) {
                p.getVantagensEscritas().add("Especialidade da Classe: " + p.getClasse().getEspecialidades());
            }
        }

        // 3. Popula observações de magias
        if (p.getMagias() != null) {
            for (Magia magia : p.getMagias()) {
                if (magia.getObservacao() != null && !magia.getObservacao().isEmpty()) {
                    p.getVantagensEscritas().add("Magia " + magia.getNome() + ": " + magia.getObservacao());
                }
            }
        }

        Map<String, Double> statusCalculado = new HashMap<>();

        // Inicializa com o status base
        if (p.getStatusBase() != null) {
            for (Map.Entry<String, Double> entry : p.getStatusBase().entrySet()) {
                statusCalculado.put(normalize(entry.getKey()), entry.getValue());
            }
        }

        // Se Vida base não for especificada, o padrão é 120.0. Para as demais, o padrão é 0.0.
        if (!statusCalculado.containsKey("vida")) {
            statusCalculado.put("vida", 120.0);
        }
        for (String attr : Arrays.asList("forca", "velocidade", "inteligencia", "destreza", "intelectualidade", "carisma", "cura")) {
            if (!statusCalculado.containsKey(attr)) {
                statusCalculado.put(attr, 0.0);
            }
        }

        // Aplica Modificadores de Raça
        if (p.getRaca() != null) {
            aplicarModificadoresRaca(p, p.getRaca(), statusCalculado);
        }

        // Aplica Modificadores de Classe
        if (p.getClasse() != null) {
            aplicarModificadoresClasse(p, p.getClasse(), statusCalculado);
        }

        // Aplica Modificadores de Magias
        if (p.getMagias() != null) {
            for (Magia magia : p.getMagias()) {
                aplicarModificadoresMagia(p, magia, statusCalculado);
            }
        }

        // Aplica Modificadores de Equipamentos
        if (p.getEquipamentosEquipados() != null) {
            for (Equipamento equip : p.getEquipamentosEquipados().values()) {
                if (equip != null && equip.getModificadoresStatus() != null) {
                    for (Map.Entry<String, Double> mod : equip.getModificadoresStatus().entrySet()) {
                        String attrKey = normalize(mod.getKey());
                        double valorAtual = statusCalculado.getOrDefault(attrKey, 0.0);
                        statusCalculado.put(attrKey, valorAtual + mod.getValue());
                    }
                }
            }
        }

        // Aplica Buffs/Debuffs ativos
        if (p.getBuffsAtivos() != null) {
            for (Map.Entry<String, Double> buff : p.getBuffsAtivos().entrySet()) {
                String attrKey = normalize(buff.getKey());
                double valorAtual = statusCalculado.getOrDefault(attrKey, 0.0);
                statusCalculado.put(attrKey, valorAtual + buff.getValue());
            }
        }

        // Salva os status finais calculados
        p.setStatusFinal(statusCalculado);
    }

    private static void aplicarModificadoresRaca(Personagem p, Raca raca, Map<String, Double> status) {
        String nomeRaca = normalize(raca.getRaca());

        if (nomeRaca.equals("hibrido") || nomeRaca.equals("híbrido")) {
            List<Raca> subRacas = p.getSubRacas();
            if (subRacas != null && subRacas.size() >= 2) {
                Raca raca1 = subRacas.get(0);
                Raca raca2 = subRacas.get(1);

                List<String> vant1 = raca1.getVantagens().getModificadores();
                List<String> vant2 = raca2.getVantagens().getModificadores();
                if (vant1.size() > 0) aplicarModificadorString(p, vant1.get(0), status);
                if (vant1.size() > 1) aplicarModificadorString(p, vant1.get(1), status);
                if (vant2.size() > 0) aplicarModificadorString(p, vant2.get(0), status);

                List<String> desvant1 = raca1.getDesvantagens().getModificadores();
                List<String> desvant2 = raca2.getDesvantagens().getModificadores();
                if (desvant2.size() > 0) aplicarModificadorString(p, desvant2.get(0), status);
                if (desvant2.size() > 1) aplicarModificadorString(p, desvant2.get(1), status);
                if (desvant1.size() > 0) aplicarModificadorString(p, desvant1.get(0), status);
            }
        } else {
            if (raca.getVantagens() != null && raca.getVantagens().getModificadores() != null) {
                for (String mod : raca.getVantagens().getModificadores()) {
                    aplicarModificadorString(p, mod, status);
                }
            }
            if (raca.getDesvantagens() != null && raca.getDesvantagens().getModificadores() != null) {
                for (String mod : raca.getDesvantagens().getModificadores()) {
                    aplicarModificadorString(p, mod, status);
                }
            }
        }
    }

    private static void aplicarModificadoresClasse(Personagem p, Classe classe, Map<String, Double> status) {
        if (classe.getBalanceamentoDeStatus() == null) return;

        List<String> positivos = classe.getBalanceamentoDeStatus().getPositivos();
        if (positivos != null) {
            for (String mod : positivos) {
                aplicarModificadorString(p, mod, status);
            }
        }

        List<String> negativos = classe.getBalanceamentoDeStatus().getNegativos();
        if (negativos != null) {
            for (String mod : negativos) {
                aplicarModificadorString(p, mod, status);
            }
        }
    }

    private static void aplicarModificadoresMagia(Personagem p, Magia magia, Map<String, Double> status) {
        if (magia.getModificadores() == null) return;

        List<String> positivos = magia.getModificadores().getPositivos();
        if (positivos != null) {
            for (String mod : positivos) {
                aplicarModificadorString(p, mod, status);
            }
        }

        List<String> negativos = magia.getModificadores().getNegativos();
        if (negativos != null) {
            for (String mod : negativos) {
                aplicarModificadorString(p, mod, status);
            }
        }
    }

    private static void registrarModificadorContagem(Personagem p, String modStr) {
        if (modStr == null || modStr.trim().isEmpty()) return;
        modStr = modStr.trim();
        boolean isPositive = modStr.startsWith("+");
        boolean isNegative = modStr.startsWith("-");
        if (!isPositive && !isNegative) return;

        String attrName = modStr.substring(1).trim();
        String normalizedAttr = normalize(attrName);

        AtributoContagem cont = p.getContagemModificadores().computeIfAbsent(normalizedAttr, k -> new AtributoContagem());
        if (isPositive) {
            cont.incrementarPositivos();
        } else {
            cont.incrementarNegativos();
        }
    }

    private static void aplicarModificadorString(Personagem p, String modStr, Map<String, Double> status) {
        if (modStr == null || modStr.trim().isEmpty()) return;

        modStr = modStr.trim();
        boolean isPositive = modStr.startsWith("+");
        boolean isNegative = modStr.startsWith("-");

        if (!isPositive && !isNegative) return; // Formato inválido

        String attrName = modStr.substring(1).trim();
        String normalizedAttr = normalize(attrName);
        AtributoModifier modifier = getModifier(normalizedAttr);

        // Registra o modificador na contagem
        registrarModificadorContagem(p, modStr);

        double delta = isPositive ? modifier.getPositivo() : modifier.getNegativo();
        double valorAtual = status.getOrDefault(normalizedAttr, 0.0);
        status.put(normalizedAttr, valorAtual + delta);
    }
}
