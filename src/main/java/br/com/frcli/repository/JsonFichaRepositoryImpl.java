package br.com.frcli.repository;

import br.com.frcli.manager.StatusManager;
import br.com.frcli.model.Personagem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonFichaRepositoryImpl implements FichaRepository {
    private final File baseDir;
    private final ObjectMapper mapper;

    public JsonFichaRepositoryImpl(File baseDir) {
        this.baseDir = baseDir;
        if (!this.baseDir.exists()) {
            this.baseDir.mkdirs();
        }
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.mapper.registerModule(new JavaTimeModule());
        migrarFichas();
    }

    private void migrarFichas() {
        File[] files = baseDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File f : files) {
                try {
                    com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(f);
                    boolean modificado = false;

                    // 1. Migração da moeda: soma dinheiroC ao dinheiroG e remove dinheiroC
                    if (node.has("inventario")) {
                        com.fasterxml.jackson.databind.JsonNode invNode = node.get("inventario");
                        if (invNode.isObject()) {
                            com.fasterxml.jackson.databind.node.ObjectNode inventario = (com.fasterxml.jackson.databind.node.ObjectNode) invNode;
                            if (inventario.has("dinheiroC")) {
                                double c = inventario.get("dinheiroC").asDouble();
                                double g = inventario.has("dinheiroG") ? inventario.get("dinheiroG").asDouble() : 0.0;
                                inventario.put("dinheiroG", g + c);
                                inventario.remove("dinheiroC");
                                modificado = true;
                            }
                        }
                    }

                    // Se o item tipoMoeda nas mochilas for C$, muda para G$
                    if (node.has("inventario") && node.get("inventario").has("itens")) {
                        com.fasterxml.jackson.databind.JsonNode itensNode = node.get("inventario").get("itens");
                        if (itensNode.isArray()) {
                            com.fasterxml.jackson.databind.node.ArrayNode itens = (com.fasterxml.jackson.databind.node.ArrayNode) itensNode;
                            for (com.fasterxml.jackson.databind.JsonNode item : itens) {
                                if (item.isObject() && item.has("tipoMoeda") && item.get("tipoMoeda").asText().equalsIgnoreCase("C$")) {
                                    ((com.fasterxml.jackson.databind.node.ObjectNode) item).put("tipoMoeda", "G$");
                                    modificado = true;
                                }
                            }
                        }
                    }

                    String raw = mapper.writeValueAsString(node);
                    String updated = raw.replaceAll("(?i)\"- defesa\"", "\"- vida\"")
                                        .replaceAll("(?i)\"\\+ defesa\"", "\"+ vida\"")
                                        .replaceAll("(?i)\"defesa\"", "\"vida\"")
                                        .replaceAll("(?i)\"C\\$\"", "\"G\\$\"");
                    
                    if (modificado || !raw.equals(updated)) {
                        mapper.writeValue(f, mapper.readTree(updated));
                        System.out.println("[Migração] Ficha '" + f.getName() + "' refatorada (dinheiro G$ unificado, defesa -> vida).");
                    }
                } catch (Exception e) {
                    System.err.println("[Erro Migração] Falha ao processar a ficha: " + f.getName() + " -> " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void salvar(Personagem p) {
        if (p == null || p.getNome() == null || p.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do personagem inválido.");
        }
        // Sanitiza o nome do arquivo a partir do nome do personagem
        String safeName = p.getNome().replaceAll("[^a-zA-Z0-9_-]", "_") + ".json";
        File file = new File(baseDir, safeName);
        try {
            mapper.writeValue(file, p);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar personagem: " + p.getNome(), e);
        }
    }

    @Override
    public Personagem buscarPorId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        String safeName = id.replaceAll("[^a-zA-Z0-9_-]", "_") + ".json";
        File file = new File(baseDir, safeName);
        if (!file.exists()) {
            return null;
        }
        try {
            Personagem p = mapper.readValue(file, Personagem.class);
            // Recalcula o status dinamicamente em memória logo após carregar o JSON
            StatusManager.recalcularStatus(p);
            return p;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar personagem com ID/Nome: " + id, e);
        }
    }

    @Override
    public List<Personagem> listarTodos() {
        List<Personagem> list = new ArrayList<>();
        File[] files = baseDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File f : files) {
                try {
                    Personagem p = mapper.readValue(f, Personagem.class);
                    StatusManager.recalcularStatus(p);
                    list.add(p);
                } catch (IOException e) {
                    // Ignora ou reporta arquivos corrompidos
                    System.err.println("Aviso: Falha ao carregar a ficha " + f.getName() + ": " + e.getMessage());
                }
            }
        }
        return list;
    }

    @Override
    public void deletar(String id) {
        if (id == null || id.trim().isEmpty()) {
            return;
        }
        String safeName = id.replaceAll("[^a-zA-Z0-9_-]", "_") + ".json";
        File file = new File(baseDir, safeName);
        if (file.exists()) {
            file.delete();
        }
    }
}
