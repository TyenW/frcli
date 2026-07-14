package br.com.frcli.manager;

import br.com.frcli.model.Cidade;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CidadeManager {
    private static final File FILE = new File("config/cidades.json");
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final Map<String, Cidade> cidades = new HashMap<>();

    static {
        carregar();
    }

    public static void carregar() {
        if (!FILE.exists()) {
            // Inicializa com cidades padrão
            adicionar(new Cidade("Cidade Central", "Centro", 1.0));
            adicionar(new Cidade("Porto Rico", "Litoral", 1.4));
            adicionar(new Cidade("Vila da Pobreza", "Fronteira", 0.6));
            salvar();
            return;
        }
        try {
            List<Cidade> list = mapper.readValue(FILE, new TypeReference<List<Cidade>>() {});
            cidades.clear();
            for (Cidade c : list) {
                cidades.put(c.getNome().toLowerCase(), c);
            }
        } catch (IOException e) {
            System.err.println("[CidadeManager] Erro ao carregar cidades: " + e.getMessage());
        }
    }

    public static void salvar() {
        File parent = FILE.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(FILE, new ArrayList<>(cidades.values()));
        } catch (IOException e) {
            System.err.println("[CidadeManager] Erro ao salvar cidades: " + e.getMessage());
        }
    }

    public static void adicionar(Cidade c) {
        if (c == null || c.getNome() == null) return;
        cidades.put(c.getNome().toLowerCase(), c);
        salvar();
    }

    public static Cidade obter(String nome) {
        if (nome == null) return null;
        return cidades.get(nome.toLowerCase());
    }

    public static List<Cidade> listarTodas() {
        return new ArrayList<>(cidades.values());
    }

    public static void remover(String nome) {
        if (nome == null) return;
        cidades.remove(nome.toLowerCase());
        salvar();
    }
}
