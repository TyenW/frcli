package br.com.frcli;

import br.com.frcli.model.*;
import br.com.frcli.repository.*;
import br.com.frcli.ui.ConsoleDashboard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        File workspaceDir = new File(".");
        File configDir = new File(workspaceDir, "config");
        
        // Executa a refatoração das tabelas de configuração (defesa -> vida)
        refatorarConfiguracoes(configDir);

        File racasFile = new File(configDir, "raças.json");
        File classesFile = new File(configDir, "classes.json");
        File magiasFile = new File(configDir, "magias.json");

        RpgDataLoader loader = new RpgDataLoader();
        List<Raca> racas = new ArrayList<>();
        List<Classe> classes = new ArrayList<>();
        List<Magia> magias = new ArrayList<>();

        try {
            if (racasFile.exists()) {
                racas = loader.carregarRacas(racasFile);
            }
            if (classesFile.exists()) {
                classes = loader.carregarClasses(classesFile);
            }
            if (magiasFile.exists()) {
                magias = loader.carregarMagias(magiasFile);
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar arquivos JSON do jogo: " + e.getMessage());
            e.printStackTrace();
        }

        // Inicializa o repositório de fichas
        File saveDir = new File(workspaceDir, "dados/campanha_1/fichas");
        FichaRepository repository = new JsonFichaRepositoryImpl(saveDir);

        // Inicia o painel CLI interativo
        ConsoleDashboard dashboard = new ConsoleDashboard(repository, racas, classes, magias);
        dashboard.iniciar();
    }

    private static void refatorarConfiguracoes(File configDir) {
        if (!configDir.exists()) return;
        File[] files = configDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File f : files) {
                try {
                    byte[] bytes = java.nio.file.Files.readAllBytes(f.toPath());
                    String content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                    
                    // Substitui defesa por vida de forma limpa nos modificadores
                    String updated = content.replaceAll("(?i)\"- defesa\"", "\"- vida\"")
                                            .replaceAll("(?i)\"\\+ defesa\"", "\"+ vida\"")
                                            .replaceAll("(?i)\"defesa\"", "\"vida\"")
                                            .replaceAll("(?i)\"C\\$\"", "\"G\\$\"")
                                            .replaceAll("(?i)C\\$", "G$");
                    
                    if (!content.equals(updated)) {
                        java.nio.file.Files.write(f.toPath(), updated.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        System.out.println("[Refatoração Config] Tabela '" + f.getName() + "' atualizada (defesa -> vida).");
                    }
                } catch (Exception e) {
                    System.err.println("[Erro Refatoração Config] Falha ao processar: " + f.getName() + " -> " + e.getMessage());
                }
            }
        }
    }
}
