package br.com.frcli.manager;

import br.com.frcli.model.Passiva;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PassivaFactory {
    private static final File CATALOG_FILE = new File("config/passivas_db.json");
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final Map<String, Passiva> passivaCatalog = new HashMap<>();

    static {
        carregarCatalogo();
    }

    public static void carregarCatalogo() {
        if (!CATALOG_FILE.exists()) {
            salvarCatalogo();
            return;
        }
        try {
            List<Passiva> passivas = mapper.readValue(CATALOG_FILE, new TypeReference<List<Passiva>>() {});
            passivaCatalog.clear();
            for (Passiva p : passivas) {
                if (p.getNome() != null) {
                    passivaCatalog.put(p.getNome().toLowerCase(), p);
                }
            }
        } catch (IOException e) {
            System.err.println("[PassivaFactory] Erro ao carregar passivas_db.json: " + e.getMessage());
        }
    }

    public static void salvarCatalogo() {
        File parent = CATALOG_FILE.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try {
            List<Passiva> passivas = new ArrayList<>(passivaCatalog.values());
            mapper.writerFor(new TypeReference<List<Passiva>>() {})
                  .withDefaultPrettyPrinter()
                  .writeValue(CATALOG_FILE, passivas);
        } catch (IOException e) {
            System.err.println("[PassivaFactory] Erro ao salvar passivas_db.json: " + e.getMessage());
        }
    }

    public static void adicionarAoCatalogo(Passiva p) {
        if (p == null || p.getNome() == null) return;
        passivaCatalog.put(p.getNome().toLowerCase(), p);
        salvarCatalogo();
    }

    public static boolean removerDoCatalogo(String nome) {
        if (nome == null) return false;
        Passiva removido = passivaCatalog.remove(nome.toLowerCase());
        if (removido != null) {
            salvarCatalogo();
            return true;
        }
        return false;
    }

    public static void atualizarNoCatalogo(String nomeAntigo, Passiva passivaAtualizada) {
        if (nomeAntigo == null || passivaAtualizada == null || passivaAtualizada.getNome() == null) return;
        passivaCatalog.remove(nomeAntigo.toLowerCase());
        passivaCatalog.put(passivaAtualizada.getNome().toLowerCase(), passivaAtualizada);
        salvarCatalogo();
    }

    public static List<Passiva> listarCatalogo() {
        return new ArrayList<>(passivaCatalog.values());
    }

    public static Passiva obterPassivaDoCatalogo(String nome) {
        if (nome == null) return null;
        Passiva original = passivaCatalog.get(nome.toLowerCase());
        if (original == null) return null;
        return clonarPassiva(original);
    }

    public static Passiva clonarPassiva(Passiva original) {
        try {
            String json = mapper.writeValueAsString(original);
            return mapper.readValue(json, Passiva.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao clonar passiva: " + e.getMessage(), e);
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
                String condicao = parts[3].trim();
                String efeito = parts[4].trim();

                if (passivaCatalog.containsKey(nome.toLowerCase())) {
                    System.out.printf("⚠️ Ignorando passiva '%s': já existe no catálogo.\n", nome);
                    continue;
                }

                Passiva p = new Passiva(nome, descricao, tipo, condicao, efeito);
                passivaCatalog.put(nome.toLowerCase(), p);
                count++;
            }
        }
        if (count > 0) {
            salvarCatalogo();
        }
        return count;
    }
}
