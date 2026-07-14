package br.com.frcli.manager;

import br.com.frcli.model.Ataque;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AtaqueFactory {
    private static final File CATALOG_FILE = new File("config/ataques_db.json");
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final Map<String, Ataque> ataqueCatalog = new HashMap<>();

    static {
        carregarCatalogo();
    }

    public static void carregarCatalogo() {
        if (!CATALOG_FILE.exists()) {
            salvarCatalogo();
            return;
        }
        try {
            List<Ataque> ataques = mapper.readValue(CATALOG_FILE, new TypeReference<List<Ataque>>() {});
            ataqueCatalog.clear();
            for (Ataque a : ataques) {
                if (a.getNome() != null) {
                    ataqueCatalog.put(a.getNome().toLowerCase(), a);
                }
            }
        } catch (IOException e) {
            System.err.println("[AtaqueFactory] Erro ao carregar ataques_db.json: " + e.getMessage());
        }
    }

    public static void salvarCatalogo() {
        File parent = CATALOG_FILE.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try {
            List<Ataque> ataques = new ArrayList<>(ataqueCatalog.values());
            mapper.writerFor(new TypeReference<List<Ataque>>() {})
                  .withDefaultPrettyPrinter()
                  .writeValue(CATALOG_FILE, ataques);
        } catch (IOException e) {
            System.err.println("[AtaqueFactory] Erro ao salvar ataques_db.json: " + e.getMessage());
        }
    }

    public static void adicionarAoCatalogo(Ataque a) {
        if (a == null || a.getNome() == null) return;
        ataqueCatalog.put(a.getNome().toLowerCase(), a);
        salvarCatalogo();
    }

    public static boolean removerDoCatalogo(String nome) {
        if (nome == null) return false;
        Ataque removido = ataqueCatalog.remove(nome.toLowerCase());
        if (removido != null) {
            salvarCatalogo();
            return true;
        }
        return false;
    }

    public static void atualizarNoCatalogo(String nomeAntigo, Ataque ataqueAtualizado) {
        if (nomeAntigo == null || ataqueAtualizado == null || ataqueAtualizado.getNome() == null) return;
        ataqueCatalog.remove(nomeAntigo.toLowerCase());
        ataqueCatalog.put(ataqueAtualizado.getNome().toLowerCase(), ataqueAtualizado);
        salvarCatalogo();
    }

    public static List<Ataque> listarCatalogo() {
        return new ArrayList<>(ataqueCatalog.values());
    }

    public static Ataque obterAtaqueDoCatalogo(String nome) {
        if (nome == null) return null;
        Ataque original = ataqueCatalog.get(nome.toLowerCase());
        if (original == null) return null;
        return clonarAtaque(original);
    }

    public static Ataque clonarAtaque(Ataque original) {
        try {
            String json = mapper.writeValueAsString(original);
            return mapper.readValue(json, Ataque.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao clonar ataque: " + e.getMessage(), e);
        }
    }

    public static int importarCSV(File csvFile) throws IOException {
        int count = 0;
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(";");
                if (parts.length < 5) continue;

                String nome = parts[0].trim();
                String descricao = parts[1].trim();
                String tipo = parts[2].trim();
                String dano = parts[3].trim();
                int nivel;
                try {
                    nivel = Integer.parseInt(parts[4].trim());
                } catch (NumberFormatException e) {
                    nivel = 1;
                }

                if (ataqueCatalog.containsKey(nome.toLowerCase())) {
                    System.out.printf("⚠️ Ignorando ataque '%s': já existe no catálogo.\n", nome);
                    continue;
                }

                Ataque a = new Ataque(nome, descricao, tipo, dano, new ArrayList<>(), new ArrayList<>(), nivel);
                ataqueCatalog.put(nome.toLowerCase(), a);
                count++;
            }
        }
        if (count > 0) {
            salvarCatalogo();
        }
        return count;
    }
}
