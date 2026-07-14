package br.com.frcli.manager;

import br.com.frcli.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ItemFactory {
    private static final File CATALOG_FILE = new File("config/itens_db.json");
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final Map<String, Item> itemCatalog = new HashMap<>();

    static {
        carregarCatalogo();
    }

    private static void repararCatalogoSeNecessario() {
        if (!CATALOG_FILE.exists()) return;
        try {
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(CATALOG_FILE);
            if (root.isArray()) {
                boolean modificado = false;
                for (com.fasterxml.jackson.databind.JsonNode node : root) {
                    if (node.isObject() && !node.has("tipoItem")) {
                        com.fasterxml.jackson.databind.node.ObjectNode obj = (com.fasterxml.jackson.databind.node.ObjectNode) node;
                        if (obj.has("slotCompativel")) {
                            obj.put("tipoItem", "EQUIPAMENTO");
                            modificado = true;
                        } else if (obj.has("quantidadeCargas")) {
                            obj.put("tipoItem", "CONSUMIVEL");
                            modificado = true;
                        }
                    }
                }
                if (modificado) {
                    System.out.println("[ItemFactory] Reparando config/itens_db.json (adicionando tipoItem)...");
                    mapper.writerWithDefaultPrettyPrinter().writeValue(CATALOG_FILE, root);
                }
            }
        } catch (Exception e) {
            System.err.println("[ItemFactory] Falha ao tentar reparar itens_db.json: " + e.getMessage());
        }
    }

    public static void carregarCatalogo() {
        repararCatalogoSeNecessario();
        if (!CATALOG_FILE.exists()) {
            salvarCatalogo();
            return;
        }
        try {
            List<Item> itens = mapper.readValue(CATALOG_FILE, new TypeReference<List<Item>>() {});
            itemCatalog.clear();
            boolean precisaSalvar = false;
            for (Item item : itens) {
                if (item.getRaridade() == null) {
                    item.setRaridade(Raridade.COMUM);
                    precisaSalvar = true;
                }
                if (item.getPoder() == null || item.getPoder() <= 0) {
                    item.setPoder(ItemPowerCalculator.calcularPoder(item));
                    precisaSalvar = true;
                }
                if (item.getValorComercial() <= 0.0) {
                    item.setValorComercial(PricingEngine.precoBaseSugerido(item));
                    precisaSalvar = true;
                }
                itemCatalog.put(item.getNome().toLowerCase(), item);
            }
            if (precisaSalvar) {
                System.out.println("[ItemFactory] Migrando campos de poder/raridade/preço nos itens carregados...");
                salvarCatalogo();
            }
        } catch (IOException e) {
            System.err.println("[ItemFactory] Erro ao carregar itens_db.json: " + e.getMessage());
        }
    }

    public static void salvarCatalogo() {
        File parent = CATALOG_FILE.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try {
            List<Item> itens = new ArrayList<>(itemCatalog.values());
            mapper.writerFor(new TypeReference<List<Item>>() {})
                  .withDefaultPrettyPrinter()
                  .writeValue(CATALOG_FILE, itens);
        } catch (IOException e) {
            System.err.println("[ItemFactory] Erro ao salvar itens_db.json: " + e.getMessage());
        }
    }

    public static void adicionarAoCatalogo(Item item) {
        if (item == null || item.getNome() == null) return;
        if (item.getRaridade() == null) {
            item.setRaridade(Raridade.COMUM);
        }
        if (item.getPoder() == null || item.getPoder() <= 0) {
            item.setPoder(ItemPowerCalculator.calcularPoder(item));
        }
        if (item.getValorComercial() <= 0.0) {
            item.setValorComercial(PricingEngine.precoBaseSugerido(item));
        }
        itemCatalog.put(item.getNome().toLowerCase(), item);
        salvarCatalogo();
    }

    public static Item obterItemDoCatalogo(String nome) {
        if (nome == null) return null;
        Item original = itemCatalog.get(nome.toLowerCase());
        if (original == null) return null;
        return clonarItem(original);
    }

    public static List<Item> listarCatalogo() {
        return new ArrayList<>(itemCatalog.values());
    }

    public static boolean removerDoCatalogo(String nome) {
        if (nome == null) return false;
        Item removido = itemCatalog.remove(nome.toLowerCase());
        if (removido != null) {
            salvarCatalogo();
            return true;
        }
        return false;
    }

    public static void atualizarNoCatalogo(String nomeAntigo, Item itemAtualizado) {
        if (nomeAntigo == null || itemAtualizado == null || itemAtualizado.getNome() == null) return;
        itemCatalog.remove(nomeAntigo.toLowerCase());
        itemCatalog.put(itemAtualizado.getNome().toLowerCase(), itemAtualizado);
        salvarCatalogo();
    }

    public static Item clonarItem(Item original) {
        try {
            String json = mapper.writeValueAsString(original);
            return mapper.readValue(json, Item.class);
        } catch (Exception e) {
            System.err.println("[ItemFactory] Erro ao clonar item: " + e.getMessage());
            return null;
        }
    }

    public static int importarCSV(File csvFile) throws IOException {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(";");
                if (parts.length < 5) continue;

                String nome = parts[0].trim();
                String descricao = parts[1].trim();
                double valorComercial;
                try {
                    valorComercial = Double.parseDouble(parts[2].trim());
                } catch (NumberFormatException e) {
                    continue;
                }
                String tipoItem = parts[3].trim().toUpperCase();
                String slotOuCargas = parts[4].trim();

                // Verifica duplicatas ou itens semelhantes no catálogo existente
                boolean itemSemelhanteExiste = false;
                String nomeSemelhante = "";
                double maiorSemelhanca = 0.0;
                
                for (String nomeExistente : itemCatalog.keySet()) {
                    double semelhanca = calcularSemelhanca(nome, nomeExistente);
                    if (semelhanca >= 0.80) { // Limiar de 80% de semelhança
                        itemSemelhanteExiste = true;
                        if (semelhanca > maiorSemelhanca) {
                            maiorSemelhanca = semelhanca;
                            nomeSemelhante = itemCatalog.get(nomeExistente).getNome();
                        }
                    }
                }
                
                if (itemSemelhanteExiste) {
                    System.out.printf("⚠️ Ignorando item '%s': muito semelhante a '%s' (Semelhança: %.1f%%)\n", 
                        nome, nomeSemelhante, maiorSemelhanca * 100.0);
                    continue;
                }

                Item novoItem;

                if (tipoItem.equals("EQUIPAMENTO")) {
                    SlotType slot = SlotType.valueOf(slotOuCargas.toUpperCase());
                    Map<String, Double> mods = new HashMap<>();
                    if (parts.length >= 6 && !parts[5].trim().isEmpty()) {
                        String[] modPairs = parts[5].split(",");
                        for (String pair : modPairs) {
                            String[] kv = pair.split(":");
                            if (kv.length == 2) {
                                mods.put(kv[0].trim(), Double.parseDouble(kv[1].trim()));
                            }
                        }
                    }

                    List<Habilidade> habs = new ArrayList<>();
                    if (parts.length >= 7 && !parts[6].trim().isEmpty()) {
                        String[] habSpecs = parts[6].split(",");
                        for (String spec : habSpecs) {
                            String[] fields = spec.split(":");
                            if (fields.length >= 2) {
                                String habNome = fields[0].trim();
                                String habTipo = fields[1].trim().toUpperCase();
                                String habDesc = fields.length >= 3 ? fields[2].trim() : "";
                                habs.add(new Habilidade(habNome, habTipo, habDesc));
                            }
                        }
                    }

                    Equipamento equipamento = new Equipamento(nome, descricao, valorComercial, "G$", slot, mods, habs);
                    if (parts.length >= 8 && !parts[7].trim().isEmpty()) {
                        equipamento.setDano(Double.parseDouble(parts[7].trim()));
                    }
                    if (parts.length >= 9 && !parts[8].trim().isEmpty()) {
                        equipamento.setTipoMunicao(parts[8].trim());
                    }
                    if (parts.length >= 10 && !parts[9].trim().isEmpty()) {
                        equipamento.setQuantidadeMunicao(Integer.parseInt(parts[9].trim()));
                    }
                    novoItem = equipamento;
                } else {
                    int cargas = Integer.parseInt(slotOuCargas);
                    novoItem = new ItemConsumivel(nome, descricao, valorComercial, "G$", cargas);
                }

                adicionarAoCatalogo(novoItem);
                count++;
            }
        }
        return count;
    }

    private static double calcularSemelhanca(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        s1 = s1.trim().toLowerCase();
        s2 = s2.trim().toLowerCase();
        if (s1.equals(s2)) return 1.0;
        
        int len1 = s1.length();
        int len2 = s2.length();
        if (len1 == 0 || len2 == 0) return 0.0;
        
        int [][] dp = new int[len1 + 1][len2 + 1];
        
        for (int i = 0; i <= len1; i++) dp[i][0] = i;
        for (int j = 0; j <= len2; j++) dp[0][j] = j;
        
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                    dp[i - 1][j] + 1,      // Deletion
                    dp[i][j - 1] + 1),     // Insertion
                    dp[i - 1][j - 1] + cost // Substitution
                );
            }
        }
        
        int maxLen = Math.max(len1, len2);
        return 1.0 - ((double) dp[len1][len2] / maxLen);
    }
}
