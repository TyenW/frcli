package br.com.frcli.manager;

import br.com.frcli.model.Loja;
import br.com.frcli.model.RedeLoja;
import br.com.frcli.model.SedeRede;
import br.com.frcli.model.LojaIndividual;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LojaManager {
    private static final File LOJAS_FILE = new File("config/lojas.json");
    private static final File REDES_FILE = new File("config/redes.json");
    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    
    private static final Map<String, Loja> lojas = new HashMap<>();
    private static final Map<String, RedeLoja> redes = new HashMap<>();

    static {
        carregar();
    }

    public static void carregar() {
        // Carrega Redes
        if (REDES_FILE.exists()) {
            try {
                List<RedeLoja> list = mapper.readValue(REDES_FILE, new TypeReference<List<RedeLoja>>() {});
                redes.clear();
                for (RedeLoja r : list) {
                    redes.put(r.getId().toLowerCase(), r);
                }
            } catch (IOException e) {
                System.err.println("[LojaManager] Erro ao carregar redes: " + e.getMessage());
            }
        } else {
            RedeLoja r = new RedeLoja("rede_mercado", "Mercadão Imperial", new ArrayList<>());
            redes.put(r.getId().toLowerCase(), r);
            salvarRedes();
        }

        // Carrega Lojas
        if (LOJAS_FILE.exists()) {
            try {
                List<Loja> list = mapper.readValue(LOJAS_FILE, new TypeReference<List<Loja>>() {});
                lojas.clear();
                for (Loja l : list) {
                    lojas.put(l.getId().toLowerCase(), l);
                }
            } catch (IOException e) {
                System.err.println("[LojaManager] Erro ao carregar lojas: " + e.getMessage());
            }
        } else {
            SedeRede s = new SedeRede("sede_central", "Sede Central de Suprimentos", "Cidade Central", 5000.0, "rede_mercado");
            LojaIndividual individual = new LojaIndividual("ferraria_grom", "Ferraria do Grom", "Cidade Central", 1500.0, 0.35, "Grom, o Ferreiro");
            
            s.getEstoque().put("adaga de ferro", 5);
            s.getEstoque().put("pocao de vida", 10);
            individual.getEstoque().put("espada longa", 2);
            individual.getEstoque().put("escudo de madeira", 3);

            adicionarLoja(s);
            adicionarLoja(individual);
            
            RedeLoja r = obterRede("rede_mercado");
            if (r != null) {
                r.getSedeIds().add(s.getId());
                salvarRedes();
            }
        }
    }

    public static void salvarLojas() {
        File parent = LOJAS_FILE.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try {
            mapper.writerFor(new TypeReference<List<Loja>>() {})
                  .withDefaultPrettyPrinter()
                  .writeValue(LOJAS_FILE, new ArrayList<>(lojas.values()));
        } catch (IOException e) {
            System.err.println("[LojaManager] Erro ao salvar lojas: " + e.getMessage());
        }
    }

    public static void salvarRedes() {
        File parent = REDES_FILE.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(REDES_FILE, new ArrayList<>(redes.values()));
        } catch (IOException e) {
            System.err.println("[LojaManager] Erro ao salvar redes: " + e.getMessage());
        }
    }

    public static void adicionarLoja(Loja l) {
        if (l == null || l.getId() == null) return;
        l.setPoder(ShopPowerCalculator.calcularPoder(l));
        lojas.put(l.getId().toLowerCase(), l);
        salvarLojas();
    }

    public static Loja obterLoja(String id) {
        if (id == null) return null;
        return lojas.get(id.toLowerCase());
    }

    public static List<Loja> listarTodasLojas() {
        return new ArrayList<>(lojas.values());
    }

    public static void removerLoja(String id) {
        if (id == null) return;
        Loja l = lojas.remove(id.toLowerCase());
        if (l != null && l instanceof SedeRede) {
            SedeRede sr = (SedeRede) l;
            RedeLoja rl = obterRede(sr.getRedeId());
            if (rl != null) {
                rl.getSedeIds().remove(l.getId());
                salvarRedes();
            }
        }
        salvarLojas();
    }

    public static void adicionarRede(RedeLoja r) {
        if (r == null || r.getId() == null) return;
        redes.put(r.getId().toLowerCase(), r);
        salvarRedes();
    }

    public static RedeLoja obterRede(String id) {
        if (id == null) return null;
        return redes.get(id.toLowerCase());
    }

    public static List<RedeLoja> listarTodasRedes() {
        return new ArrayList<>(redes.values());
    }

    public static void removerRede(String id) {
        if (id == null) return;
        redes.remove(id.toLowerCase());
        salvarRedes();
    }
}
