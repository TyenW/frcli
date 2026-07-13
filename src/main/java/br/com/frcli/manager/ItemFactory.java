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

    public static void carregarCatalogo() {
        if (!CATALOG_FILE.exists()) {
            salvarCatalogo();
            return;
        }
        try {
            List<Item> itens = mapper.readValue(CATALOG_FILE, new TypeReference<List<Item>>() {});
            itemCatalog.clear();
            for (Item item : itens) {
                itemCatalog.put(item.getNome().toLowerCase(), item);
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
            mapper.writerWithDefaultPrettyPrinter().writeValue(CATALOG_FILE, itens);
        } catch (IOException e) {
            System.err.println("[ItemFactory] Erro ao salvar itens_db.json: " + e.getMessage());
        }
    }

    public static void adicionarAoCatalogo(Item item) {
        if (item == null || item.getNome() == null) return;
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
                double valorComercial = Double.parseDouble(parts[2].trim());
                String tipoItem = parts[3].trim().toUpperCase();
                String slotOuCargas = parts[4].trim();

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

                    novoItem = new Equipamento(nome, descricao, valorComercial, "G$", slot, mods, habs);
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
}
