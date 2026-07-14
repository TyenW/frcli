package br.com.frcli.ui;

import br.com.frcli.manager.*;
import br.com.frcli.model.*;
import br.com.frcli.event.*;
import br.com.frcli.repository.FichaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

import java.util.*;

public class ConsoleDashboard {
    private final FichaRepository repository;
    private final List<Raca> racas;
    private final List<Classe> classes;
    private final List<Magia> magias;
    private List<Monstro> monstros = new ArrayList<>();

    public ConsoleDashboard(FichaRepository repository, List<Raca> racas, List<Classe> classes, List<Magia> magias) {
        this.repository = repository;
        this.racas = racas != null ? racas : new ArrayList<>();
        this.classes = classes != null ? classes : new ArrayList<>();
        this.magias = magias != null ? magias : new ArrayList<>();
        carregarMonstros();
    }

    public void iniciar() {
        while (true) {
            MenuBuilder menu = new MenuBuilder("🎲 PAINEL DE GERENCIAMENTO RPG 🎲");
            menu.addItem(1, "Gerenciar Fichas", "Personagens")
                .addItem(2, "Inventário e Equipamentos", "Mochila & Armadura")
                .addItem(3, "Magias e Conhecimento", "Gerenciar magias do personagem")
                .addItem(4, "Tabelas Globais", "Raças, Classes, Magias")
                .addItem(5, "Comércio e Lojas 🏪", "Cidades, Redes e Lojas")
                .addItem(6, "Importação e Criação de Itens", "Importar CSV ou criar item catalogo")
                .addItem(7, "Gerenciar Catálogo de Ataques", "CRUD e Evoluções de Ataques")
                .addItem(0, "Sair", "Encerrar jogo");

            int opcao = menu.getUserChoice(0, 7);

            switch (opcao) {
                case 1:
                    menuPersonagens();
                    break;
                case 2:
                    menuEquipamentos();
                    break;
                case 3:
                    menuMagiasCombate();
                    break;
                case 4:
                    menuConfiguracoes();
                    break;
                case 5:
                    menuComercioLojas();
                    break;
                case 6:
                    menuImportacaoItens();
                    break;
                case 7:
                    menuAtaques();
                    break;
                case 0:
                    UiFormatter.printBlank();
                    UiFormatter.printSuccess("Encerrando o sistema RPG. Até a próxima aventura! ⚔️");
                    UiFormatter.printBlank();
                    return;
            }
        }
    }

    // ==========================================
    // MENU DE PERSONAGENS
    // ==========================================
    private void menuPersonagens() {
        while (true) {
            MenuBuilder menu = new MenuBuilder("👥 GERENCIAR PERSONAGENS 👥");
            menu.addItem(1, "Criar Nova Ficha", "Novo personagem")
                .addItem(2, "Listar Fichas Cadastradas", "Ver todos")
                .addItem(3, "Visualizar Detalhes", "Ficha completa")
                .addItem(4, "Alterar Atributos", "Buffs e Status")
                .addItem(5, "Excluir Ficha", "Deletar permanentemente")
                .addItem(0, "Voltar", "Menu anterior");

            int opcao = menu.getUserChoice(0, 5);

            switch (opcao) {
                case 1:
                    criarPersonagem();
                    break;
                case 2:
                    listarPersonagens();
                    InputUtil.pressEnterToContinue();
                    break;
                case 3:
                    visualizarFicha();
                    break;
                case 4:
                    alterarPersonagem();
                    break;
                case 5:
                    deletarPersonagem();
                    break;
                case 0:
                    return;
            }
        }
    }

    private void listarPersonagens() {
        List<Personagem> lista = repository.listarTodos();
        if (lista.isEmpty()) {
            UiFormatter.printWarning("Nenhum personagem cadastrado no momento.");
            return;
        }
        
        UiFormatter.printSubtitle("FICHAS SALVAS EM DISCO");
        for (int i = 0; i < lista.size(); i++) {
            Personagem p = lista.get(i);
            String raca = p.getRaca() != null ? p.getRaca().getRaca() : "Indefinida";
            String classe = p.getClasse() != null ? p.getClasse().getClasse() : "Indefinida";
            System.out.printf("  %d. %s %s(%s %s) - Tier %d%s\n", 
                i + 1, 
                UiFormatter.BOLD + p.getNome() + UiFormatter.RESET,
                UiFormatter.BLUE,
                raca, classe,
                p.getTierCampanha(),
                UiFormatter.RESET);
        }
    }

    private Personagem selecionarPersonagem() {
        List<Personagem> lista = repository.listarTodos();
        if (lista.isEmpty()) {
            System.out.println("Nenhum personagem cadastrado no momento.");
            return null;
        }
        listarPersonagens();
        int index = InputUtil.readInt("Selecione o número do personagem: ", 1, lista.size());
        return lista.get(index - 1);
    }

    private void criarPersonagem() {
        UiFormatter.printSubtitle("CRIAR NOVO PERSONAGEM");
        String nome = InputUtil.readStringNotEmpty("Nome do Personagem: ");

        // Verifica se já existe
        if (repository.buscarPorId(nome) != null) {
            UiFormatter.printError("Já existe um personagem com este nome!");
            return;
        }

        // Seleção de Raça
        if (racas.isEmpty()) {
            UiFormatter.printError("Nenhuma raça carregada no sistema global. Impossível criar personagem.");
            return;
        }
        System.out.println("Escolha a Raça:");
        for (int i = 0; i < racas.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, racas.get(i).getRaca());
        }
        System.out.println("0. Voltar");
        int indexRaca = InputUtil.readInt("Opção (ou 0 para Cancelar): ", 0, racas.size());
        if (indexRaca == 0) return;
        Raca racaEscolhida = racas.get(indexRaca - 1);

        List<Raca> subRacas = new ArrayList<>();
        if (racaEscolhida.getRaca().equalsIgnoreCase("Hibrido") || racaEscolhida.getRaca().equalsIgnoreCase("Híbrido")) {
            System.out.println("Selecione as 2 sub-raças do Híbrido:");
            for (int k = 1; k <= 2; k++) {
                System.out.println("Escolha a sub-raça " + k + ":");
                for (int i = 0; i < racas.size(); i++) {
                    if (racas.get(i).getRaca().equalsIgnoreCase("Hibrido") || racas.get(i).getRaca().equalsIgnoreCase("Híbrido")) {
                        continue;
                    }
                    System.out.printf("%d. %s\n", i + 1, racas.get(i).getRaca());
                }
                System.out.println("0. Voltar (Cancela Criação)");
                int idxSub = InputUtil.readInt("Opção (ou 0 para Cancelar): ", 0, racas.size());
                if (idxSub == 0) return;
                subRacas.add(racas.get(idxSub - 1));
            }
        }

        // Seleção de Classe (Opcional)
        Classe classeEscolhida = null;
        if (!classes.isEmpty() && InputUtil.readBoolean("Deseja atribuir uma classe ao personagem? (s/n): ")) {
            System.out.println("Escolha a Classe:");
            for (int i = 0; i < classes.size(); i++) {
                System.out.printf("%d. %s (Mochila: %s)\n", i + 1, classes.get(i).getClasse(), classes.get(i).getTamanhoDaMochila());
            }
            System.out.println("0. Voltar (Sem classe)");
            int indexClasse = InputUtil.readInt("Opção (ou 0 para Sem Classe): ", 0, classes.size());
            if (indexClasse > 0) {
                classeEscolhida = classes.get(indexClasse - 1);
            }
        }

        // Identidade extra
        Integer idade = InputUtil.readBoolean("Inserir Idade? (s/n): ") ? InputUtil.readInt("Idade: ") : null;
        Double tamanho = InputUtil.readBoolean("Inserir Tamanho (Altura)? (s/n): ") ? InputUtil.readDouble("Tamanho (metros): ") : null;
        String historia = InputUtil.readString("História / Descrição: ");

        // Cria Personagem
        Personagem p = new Personagem();
        p.setNome(nome);
        p.setIdade(idade);
        p.setTamanho(tamanho);
        p.setHistoria(historia);
        p.setRaca(racaEscolhida);
        p.setSubRacas(subRacas);
        p.setClasse(classeEscolhida);

        // Atributos Base
        Map<String, Double> statusBase = new HashMap<>();
        statusBase.put("vida", 120.0); // Padrão
        
        boolean customAttr = InputUtil.readBoolean("Deseja personalizar os atributos base (padrão é 0.0)? (s/n): ");
        if (customAttr) {
            statusBase.put("forca", InputUtil.readDouble("Força Base (padrão 0.0): "));
            statusBase.put("velocidade", InputUtil.readDouble("Velocidade Base (padrão 0.0): "));
            statusBase.put("inteligencia", InputUtil.readDouble("Inteligência Base (padrão 0.0): "));
            statusBase.put("destreza", InputUtil.readDouble("Destreza Base (padrão 0.0): "));
            statusBase.put("intelectualidade", InputUtil.readDouble("Intelectualidade Base (padrão 0.0): "));
            statusBase.put("carisma", InputUtil.readDouble("Carisma Base (padrão 0.0): "));
        } else {
            statusBase.put("forca", 0.0);
            statusBase.put("velocidade", 0.0);
            statusBase.put("inteligencia", 0.0);
            statusBase.put("destreza", 0.0);
            statusBase.put("intelectualidade", 0.0);
            statusBase.put("carisma", 0.0);
        }
        p.setStatusBase(statusBase);

        // Inicializa Mochila baseado no tamanho da mochila da classe ou escolha manual
        MochilaType mochilaType = MochilaType.PEQUENA;
        if (classeEscolhida != null) {
            String mochilaStr = classeEscolhida.getTamanhoDaMochila().toLowerCase();
            if (mochilaStr.contains("grande")) {
                mochilaType = MochilaType.GRANDE;
            } else if (mochilaStr.contains("medi") || mochilaStr.contains("média")) {
                mochilaType = MochilaType.MEDIA;
            }
        } else {
            System.out.println("Selecione o tamanho da mochila para este personagem sem classe:");
            System.out.println("1. Pequena");
            System.out.println("2. Média");
            System.out.println("3. Grande");
            int choiceMochila = InputUtil.readInt("Opção: ", 1, 3);
            if (choiceMochila == 2) {
                mochilaType = MochilaType.MEDIA;
            } else if (choiceMochila == 3) {
                mochilaType = MochilaType.GRANDE;
            }
        }
        Mochila mochila = new Mochila(mochilaType, new ArrayList<>(), 100.0); // Moeda inicial G$ 100
        p.setInventario(mochila);

        // Permite adicionar itens iniciais se desejado
        while (InputUtil.readBoolean("Deseja adicionar um item inicial à mochila agora? (s/n): ")) {
            adicionarItemMochila(p);
        }

        // Recalcula e Salva
        StatusManager.recalcularStatus(p);
        repository.salvar(p);

        UiFormatter.printBlank();
        UiFormatter.printSuccess("Personagem '" + nome + "' criado com sucesso! ⚔️");
        exibirFicha(p);
        InputUtil.pressEnterToContinue();
    }

    private void visualizarFicha() {
        Personagem p = selecionarPersonagem();
        if (p == null) return;
        exibirFicha(p);
        InputUtil.pressEnterToContinue();
    }

    private void exibirFicha(Personagem p) {
        UiFormatter.printTitle("📋 FICHA: " + p.getNome().toUpperCase());
        
        // Informações básicas
        UiFormatter.printSection("INFORMAÇÕES BÁSICAS");
        UiFormatter.printItem("Idade", p.getIdade() != null ? p.getIdade() + " anos" : "Não informado");
        UiFormatter.printItem("Tamanho", p.getTamanho() != null ? String.format("%.2f m", p.getTamanho()) : "Não informado");
        UiFormatter.printItem("Raça", p.getRaca() != null ? p.getRaca().getRaca() : "Sem raça");
        if (!p.getSubRacas().isEmpty()) {
            UiFormatter.printItem("Sub-raças (Híbrido)", 
                p.getSubRacas().get(0).getRaca() + " / " + p.getSubRacas().get(1).getRaca());
        }
        UiFormatter.printItem("Classe", p.getClasse() != null ? p.getClasse().getClasse() : "Sem classe");
        UiFormatter.printItem("Tier", String.valueOf(p.getTierCampanha()));
        UiFormatter.endSection();
        
        // História
        if (p.getHistoria() != null && !p.getHistoria().isEmpty()) {
            UiFormatter.printSection("HISTÓRIA");
            System.out.println("  " + p.getHistoria());
            UiFormatter.endSection();
        }
        
        // Atributos
        UiFormatter.printSection("ATRIBUTOS (Base → Final)");
        for (String attr : Arrays.asList("vida", "forca", "velocidade", "inteligencia", "destreza", "intelectualidade", "carisma", "cura")) {
            double base = p.getStatusBase().getOrDefault(attr, 0.0);
            double fim = p.getStatusFinalAtributo(attr);
            
            int positivos = 0, negativos = 0;
            if (p.getContagemModificadores() != null && p.getContagemModificadores().containsKey(attr)) {
                AtributoContagem cont = p.getContagemModificadores().get(attr);
                positivos = cont.getPositivos();
                negativos = cont.getNegativos();
            }
            
            String attrName = attr.substring(0, 1).toUpperCase() + attr.substring(1);
            System.out.printf("  %-18s: %6.1f → %6.1f %s(+%d -%d)%s\n", 
                attrName, base, fim, UiFormatter.BLUE, positivos, negativos, UiFormatter.RESET);
        }
        UiFormatter.endSection();
        
        // Características
        UiFormatter.printSection("CARACTERÍSTICAS ESCRITAS");
        if (p.getVantagensEscritas() != null && !p.getVantagensEscritas().isEmpty()) {
            System.out.println("  " + UiFormatter.GREEN + "✓ Vantagens:" + UiFormatter.RESET);
            p.getVantagensEscritas().forEach(v -> UiFormatter.printBullet(v));
        } else {
            System.out.println("  Nenhuma vantagem descritiva");
        }
        
        if (p.getDesvantagensEscritas() != null && !p.getDesvantagensEscritas().isEmpty()) {
            System.out.println("  " + UiFormatter.RED + "✗ Desvantagens:" + UiFormatter.RESET);
            p.getDesvantagensEscritas().forEach(d -> UiFormatter.printBullet(d));
        } else {
            System.out.println("  Nenhuma desvantagem descritiva");
        }
        UiFormatter.endSection();
        
        // Mochila
        UiFormatter.printSection("MOCHILA - " + p.getInventario().getTipo());
        System.out.printf("  Itens: %d / %d\n", p.getInventario().getItens().size(), 
            p.getInventario().getMaxItens());
        System.out.printf("  Saldo Carteira: G$ %.2f / %.2f (Teto)\n", 
            p.getInventario().getDinheiroG(), 
            p.getInventario().getMaxG());
        UiFormatter.endSection();
        
        // Equipamentos
        UiFormatter.printSection("EQUIPAMENTOS EQUIPADOS");
        p.getEquipamentosEquipados().forEach((slot, equip) -> {
            String status = equip != null ? equip.getNome() + " (" + equip.getDescricao() + ")" : "[Vazio]";
            System.out.printf("  %-18s: %s\n", slot, status);
        });
        UiFormatter.endSection();
        
        // Magias
        UiFormatter.printSection("MAGIAS APRENDIDAS");
        if (p.getMagias().isEmpty()) {
            System.out.println("  Nenhuma magia aprendida");
        } else {
            p.getMagias().forEach(m -> UiFormatter.printBullet(m.getNome() + " (" + m.getNomeTraduzido() + ")"));
        }
        UiFormatter.endSection();
        
        // Habilidades
        UiFormatter.printSection("HABILIDADES DISPONÍVEIS");
        List<Habilidade> habs = CombatManager.obterAcoesDisponiveis(p);
        if (habs.isEmpty()) {
            System.out.println("  Nenhuma habilidade disponível");
        } else {
            habs.forEach(h -> UiFormatter.printBullet(h.toString()));
        }
        UiFormatter.endSection();

        // Proficiências de Magias e Armas
        UiFormatter.printSection("PROFICIÊNCIAS DE MAGIA & ARMAS");
        Map<String, Integer> profs = p.getProficiencias();
        boolean temProfs = false;
        for (Map.Entry<String, Integer> entry : profs.entrySet()) {
            if (entry.getValue() > 0) {
                String chave = entry.getKey();
                String cap = chave.substring(0, 1).toUpperCase() + chave.substring(1);
                System.out.printf("  • %-25s: Nível %d / 10\n", cap, entry.getValue());
                temProfs = true;
            }
        }
        if (!temProfs) {
            System.out.println("  Nenhuma proficiência adquirida (todas nível 0).");
        }
        UiFormatter.endSection();

        // Ataques Vinculados
        UiFormatter.printSection("ATAQUES VINCULADOS À FICHA");
        if (p.getAtaques().isEmpty()) {
            System.out.println("  Nenhum ataque atribuído.");
        } else {
            for (Ataque a : p.getAtaques()) {
                System.out.printf("  • %s [%s] (Dano: %s) [Nível: %d]\n", a.getNome(), a.getTipoMagiaOuArma(), a.getDano(), a.getNivel());
                if (a.getDescricao() != null && !a.getDescricao().isEmpty()) {
                    System.out.printf("    Descrição: %s\n", a.getDescricao());
                }
            }
        }
        UiFormatter.endSection();

        // Passivas Vinculadas
        UiFormatter.printSection("PASSIVAS VINCULADAS À FICHA");
        if (p.getPassivas().isEmpty()) {
            System.out.println("  Nenhuma passiva atribuída.");
        } else {
            for (Passiva pass : p.getPassivas()) {
                System.out.printf("  • %s [%s] - Condição: %s | Efeito: %s\n", 
                    pass.getNome(), pass.getTipoMagiaOuArma(), pass.getCondicao(), pass.getEfeito());
                if (pass.getDescricao() != null && !pass.getDescricao().isEmpty()) {
                    System.out.printf("    Descrição: %s\n", pass.getDescricao());
                }
            }
        }
        UiFormatter.endSection();
        
        System.out.println();
    }

    private void alterarPersonagem() {
        Personagem p = selecionarPersonagem();
        if (p == null) return;

        System.out.println("\n--- ALTERAR PERSONAGEM: " + p.getNome() + " ---");
        System.out.println("1. Alterar Atributos Base");
        System.out.println("2. Alterar Tier de Campanha");
        System.out.println("3. Adicionar Buff/Debuff Temporário");
        System.out.println("4. Limpar Buffs/Debuffs");
        System.out.println("5. Editar Vantagens Escritas");
        System.out.println("6. Editar Desvantagens Escritas");
        System.out.println("7. Alterar Níveis de Proficiência (0-10)");
        System.out.println("8. Gerenciar Ataques do Personagem");
        System.out.println("9. Mudar Cidade Atual do Personagem");
        System.out.println("10. Gerenciar Passivas do Personagem");
        System.out.println("0. Voltar");

        int opcao = InputUtil.readInt("Opção: ", 0, 10);

        switch (opcao) {
            case 1:
                for (String attr : p.getStatusBase().keySet()) {
                    Double novoVal = InputUtil.readOptionalDouble("Novo valor para " + attr + " (Atual: " + p.getStatusBase().get(attr) + ")");
                    if (novoVal != null) {
                        p.getStatusBase().put(attr, novoVal);
                    }
                }
                break;
            case 2:
                Integer novoTier = InputUtil.readOptionalInt("Novo Tier de Campanha (Atual: " + p.getTierCampanha() + ")");
                if (novoTier != null) {
                    if (novoTier >= 1 && novoTier <= 10) {
                        p.setTierCampanha(novoTier);
                    } else {
                        System.out.println("Erro: Tier deve ser entre 1 e 10. Não alterado.");
                    }
                }
                break;
            case 3:
                String attrName = InputUtil.readStringNotEmpty("Nome do Atributo (ex: Forca, Vida): ");
                double modificador = InputUtil.readDouble("Modificador (+/- valor): ");
                p.getBuffsAtivos().put(StatusManager.normalize(attrName), modificador);
                break;
            case 4:
                p.getBuffsAtivos().clear();
                System.out.println("Todos os buffs temporários foram limpos.");
                break;
            case 5:
                editarVantagensDesvantagens(p, true);
                return;
            case 6:
                editarVantagensDesvantagens(p, false);
                return;
            case 7:
                alterarProficienciasPersonagem(p);
                break;
            case 8:
                gerenciarAtaquesPersonagem(p);
                break;
            case 9:
                List<Cidade> todasCidades = CidadeManager.listarTodas();
                if (todasCidades.isEmpty()) {
                    System.out.println("Nenhuma cidade cadastrada!");
                    break;
                }
                System.out.println("Selecione a nova Cidade para o Personagem:");
                for (int i = 0; i < todasCidades.size(); i++) {
                    System.out.printf("%d. %s (Região: %s, Riqueza: %.1f)\n", i + 1, todasCidades.get(i).getNome(), todasCidades.get(i).getRegiao(), todasCidades.get(i).getIndiceRiqueza());
                }
                int selC = InputUtil.readInt("Opção: ", 1, todasCidades.size());
                p.setCidade(todasCidades.get(selC - 1).getNome());
                UiFormatter.printSuccess("O personagem viajou para " + p.getCidade() + "!");
                break;
            case 10:
                gerenciarPassivasPersonagem(p);
                break;
            case 0:
                return;
        }

        StatusManager.recalcularStatus(p);
        repository.salvar(p);
        System.out.println("Alterações aplicadas e salvas com sucesso!");
    }

    private void alterarProficienciasPersonagem(Personagem p) {
        while (true) {
            UiFormatter.printSubtitle("PROFICIÊNCIAS DE " + p.getNome().toUpperCase());
            Map<String, Integer> profs = p.getProficiencias();
            List<String> chavesAtivas = new ArrayList<>(profs.keySet());
            
            System.out.println("Proficiências Ativas:");
            if (chavesAtivas.isEmpty()) {
                System.out.println("  (Nenhuma proficiência ativa)");
            } else {
                for (int i = 0; i < chavesAtivas.size(); i++) {
                    String chave = chavesAtivas.get(i);
                    String cap = chave.substring(0, 1).toUpperCase() + chave.substring(1);
                    System.out.printf("  %d. %s: Nível %d\n", i + 1, cap, profs.get(chave));
                }
            }
            
            System.out.println("\nOpções:");
            System.out.println("1. Adicionar Nova Proficiência");
            System.out.println("2. Alterar Nível de uma Proficiência Ativa");
            System.out.println("0. Voltar");
            
            int opt = InputUtil.readInt("Opção: ", 0, 2);
            if (opt == 0) return;
            
            if (opt == 1) {
                List<String> disponiveis = new ArrayList<>();
                for (Magia m : magias) {
                    disponiveis.add(m.getNome());
                }
                disponiveis.add("Armas de longa distância");
                disponiveis.add("Armas de curta distância");
                
                // Filtra as já existentes na ficha (compara case-insensitive)
                disponiveis.removeIf(nome -> profs.containsKey(nome.toLowerCase()));
                
                if (disponiveis.isEmpty()) {
                    System.out.println("O personagem já possui todas as proficiências possíveis!");
                    InputUtil.pressEnterToContinue();
                    continue;
                }
                
                System.out.println("\n--- SELECIONE A PROFICIÊNCIA PARA ADICIONAR ---");
                for (int i = 0; i < disponiveis.size(); i++) {
                    System.out.printf("%d. %s\n", i + 1, disponiveis.get(i));
                }
                System.out.println("0. Voltar");
                
                int sel = InputUtil.readInt("Opção: ", 0, disponiveis.size());
                if (sel == 0) continue;
                
                String novaProf = disponiveis.get(sel - 1);
                int nivel = InputUtil.readInt("Nível da Proficiência (1 a 10): ", 1, 10);
                profs.put(novaProf.toLowerCase(), nivel);
                UiFormatter.printSuccess("Proficiência '" + novaProf + "' adicionada com Nível " + nivel + "!");
            } else if (opt == 2) {
                if (chavesAtivas.isEmpty()) {
                    System.out.println("Nenhuma proficiência para alterar.");
                    continue;
                }
                int idx = InputUtil.readInt("Selecione a proficiência: ", 1, chavesAtivas.size());
                String chaveSelecionada = chavesAtivas.get(idx - 1);
                int novoNivel = InputUtil.readInt("Novo nível (0 a 10, use 0 para remover): ", 0, 10);
                
                if (novoNivel <= 0) {
                    profs.remove(chaveSelecionada);
                    UiFormatter.printSuccess("Proficiência '" + (chaveSelecionada.substring(0, 1).toUpperCase() + chaveSelecionada.substring(1)) + "' removida!");
                } else {
                    profs.put(chaveSelecionada, novoNivel);
                    UiFormatter.printSuccess("Proficiência '" + (chaveSelecionada.substring(0, 1).toUpperCase() + chaveSelecionada.substring(1)) + "' alterada para Nível " + novoNivel + "!");
                }
            }
        }
    }

    private void gerenciarAtaquesPersonagem(Personagem p) {
        while (true) {
            UiFormatter.printSubtitle("ATAQUES DE " + p.getNome().toUpperCase());
            List<Ataque> atqs = p.getAtaques();
            if (atqs.isEmpty()) {
                System.out.println("(Nenhum ataque atribuído)");
            } else {
                for (int i = 0; i < atqs.size(); i++) {
                    Ataque a = atqs.get(i);
                    System.out.printf("%d. %s [%s] (Dano: %s)\n", i + 1, a.getNome(), a.getTipoMagiaOuArma(), a.getDano());
                }
            }
            
            System.out.println("\nOpções:");
            System.out.println("1. Adicionar Ataque do Catálogo");
            System.out.println("2. Remover Ataque do Personagem");
            System.out.println("0. Voltar");
            
            int escolha = InputUtil.readInt("Opção: ", 0, 2);
            if (escolha == 0) return;
            
            if (escolha == 1) {
                List<Ataque> catalogo = AtaqueFactory.listarCatalogo();
                if (catalogo.isEmpty()) {
                    System.out.println("Não existem ataques cadastrados no catálogo!");
                    InputUtil.pressEnterToContinue();
                    continue;
                }
                
                System.out.println("\n--- ATAQUES DISPONÍVEIS NO CATÁLOGO ---");
                for (int i = 0; i < catalogo.size(); i++) {
                    System.out.printf("%d. %s [%s]\n", i + 1, catalogo.get(i).getNome(), catalogo.get(i).getTipoMagiaOuArma());
                }
                int idx = InputUtil.readInt("Escolha o ataque para adicionar: ", 1, catalogo.size());
                Ataque selecionado = catalogo.get(idx - 1);
                
                boolean jaTem = false;
                for (Ataque a : atqs) {
                    if (a.getNome().equalsIgnoreCase(selecionado.getNome())) {
                        jaTem = true;
                        break;
                    }
                }
                
                if (jaTem) {
                    UiFormatter.printWarning("O personagem já possui este ataque!");
                } else {
                    atqs.add(AtaqueFactory.clonarAtaque(selecionado));
                    UiFormatter.printSuccess("Ataque '" + selecionado.getNome() + "' adicionado!");
                }
            } else if (escolha == 2) {
                if (atqs.isEmpty()) {
                    System.out.println("O personagem não tem ataques para remover.");
                    continue;
                }
                int idx = InputUtil.readInt("Escolha o ataque para remover: ", 1, atqs.size());
                Ataque removido = atqs.remove(idx - 1);
                UiFormatter.printSuccess("Ataque '" + removido.getNome() + "' removido!");
            }
        }
    }

    private void editarVantagensDesvantagens(Personagem p, boolean isVantagem) {
        List<String> lista = isVantagem ? p.getVantagensEscritas() : p.getDesvantagensEscritas();
        String termo = isVantagem ? "Vantagem" : "Desvantagem";
        
        while (true) {
            System.out.println("\n--- EDITAR " + termo.toUpperCase() + "S ESCRITAS ---");
            if (lista.isEmpty()) {
                System.out.println("Nenhuma " + termo.toLowerCase() + " descritiva cadastrada.");
            } else {
                for (int i = 0; i < lista.size(); i++) {
                    System.out.printf("%d. %s\n", i + 1, lista.get(i));
                }
            }
            System.out.println("\n1. Adicionar Nova " + termo);
            System.out.println("2. Excluir " + termo);
            System.out.println("0. Voltar");
            
            int op = InputUtil.readInt("Opção: ", 0, 2);
            if (op == 0) break;
            
            if (op == 1) {
                String novoTexto = InputUtil.readStringNotEmpty("Escreva a nova característica: ");
                lista.add(novoTexto);
                System.out.println("Adicionado!");
            } else if (op == 2) {
                if (lista.isEmpty()) {
                    System.out.println("Nada para excluir.");
                } else {
                    int idx = InputUtil.readInt("Selecione o número para excluir: ", 1, lista.size());
                    lista.remove(idx - 1);
                    System.out.println("Excluído!");
                }
            }
        }
        StatusManager.recalcularStatus(p);
        repository.salvar(p);
        System.out.println("Alterações de características escritas salvas!");
    }

    private void deletarPersonagem() {
        UiFormatter.printSubtitle("EXCLUIR FICHA DE PERSONAGEM");
        Personagem p = selecionarPersonagem();
        if (p == null) return;

        System.out.println(UiFormatter.RED + UiFormatter.BOLD + "⚠️ ATENÇÃO! Esta ação é permanente!" + UiFormatter.RESET);
        boolean conf = InputUtil.readBoolean("Tem certeza de que deseja deletar a ficha de " + p.getNome() + "? (s/n): ");
        if (conf) {
            repository.deletar(p.getNome());
            UiFormatter.printSuccess("Ficha excluída com sucesso!");
        } else {
            UiFormatter.printInfo("Operação cancelada.");
        }
    }

    // ==========================================
    // MENU DE EQUIPAMENTOS E INVENTÁRIO
    // ==========================================
    private void menuEquipamentos() {
        Personagem p = selecionarPersonagem();
        if (p == null) return;

        while (true) {
            MenuBuilder menu = new MenuBuilder("🎒 INVENTÁRIO & EQUIPAMENTOS: " + p.getNome().toUpperCase());
            menu.addItem(1, "Exibir Inventário", "Visualizar mochila")
                .addItem(2, "Criar e Adicionar Item", "Novo item")
                .addItem(3, "Equipar Item", "Vestir equipamento")
                .addItem(4, "Desequipar Item", "Remover equipamento")
                .addItem(5, "Editar Item", "Modificar propriedades")
                .addItem(6, "Excluir Item", "Remover permanentemente")
                .addItem(7, "Gerenciar Carteira", "Adicionar/gastar moeda")
                .addItem(0, "Voltar", "Menu anterior");

            int opcao = menu.getUserChoice(0, 7);

            switch (opcao) {
                case 1:
                    exibirFicha(p);
                    InputUtil.pressEnterToContinue();
                    break;
                case 2:
                    adicionarItemMochila(p);
                    break;
                case 3:
                    equiparItem(p);
                    break;
                case 4:
                    desequiparItem(p);
                    break;
                case 5:
                    editarItem(p);
                    break;
                case 6:
                    deletarItem(p);
                    break;
                case 7:
                    gerenciarCarteira(p);
                    break;
                case 0:
                    return;
            }
        }
    }

    private void adicionarItemMochila(Personagem p) {
        UiFormatter.printSubtitle("CRIAR NOVO ITEM");
        String nome = InputUtil.readStringNotEmpty("Nome do Item: ");
        String desc = InputUtil.readString("Descrição: ");
        double valor = InputUtil.readDouble("Valor Comercial: ", 0, Double.MAX_VALUE);
        String moeda = "G$";

        System.out.println("Selecione o tipo do item:");
        System.out.println("1. Equipamento");
        System.out.println("2. Consumível");
        System.out.println("0. Voltar");
        int tipoItem = InputUtil.readInt("Opção (ou 0 para Cancelar): ", 0, 2);
        if (tipoItem == 0) return;
        Item novoItem;

        if (tipoItem == 1) {
            System.out.println("Selecione o Slot Compatível:");
            SlotType[] slots = SlotType.values();
            for (int i = 0; i < slots.length; i++) {
                System.out.printf("%d. %s\n", i + 1, slots[i]);
            }
            System.out.println("0. Voltar");
            int idxSlot = InputUtil.readInt("Opção (ou 0 para Cancelar): ", 0, slots.length);
            if (idxSlot == 0) return;
            SlotType slot = slots[idxSlot - 1];

            // Modificadores de Atributos
            Map<String, Double> mods = new HashMap<>();
            while (InputUtil.readBoolean("Adicionar modificador de atributo a este item? (s/n): ")) {
                String attr = InputUtil.readStringNotEmpty("Nome do atributo (ex: Vida, Forca): ");
                double mod = InputUtil.readDouble("Modificador (+/- valor): ");
                mods.put(attr, mod);
            }

            // Habilidades embutidas
            List<Habilidade> habs = new ArrayList<>();
            while (InputUtil.readBoolean("Adicionar habilidade embutida a este item? (s/n): ")) {
                String habNome = InputUtil.readStringNotEmpty("Nome da Habilidade: ");
                String habTipo = InputUtil.readBoolean("Tipo da Habilidade: Ativa (s) ou Passiva (n)? ") ? "ATIVA" : "PASSIVA";
                String habDesc = InputUtil.readString("Descrição da habilidade: ");
                habs.add(new Habilidade(habNome, habTipo, habDesc));
            }

            novoItem = new Equipamento(nome, desc, valor, moeda, slot, mods, habs);
        } else {
            int cargas = InputUtil.readInt("Quantidade de Cargas / Usos: ", 1, 100);
            novoItem = new ItemConsumivel(nome, desc, valor, moeda, cargas);
        }

        try {
            InventoryManager.adicionarItem(p.getInventario(), novoItem);
            repository.salvar(p);
            UiFormatter.printSuccess("Item '" + nome + "' criado e guardado na mochila! 📦");
        } catch (MochilaCheiaException e) {
            UiFormatter.printError(e.getMessage());
        }
    }

    private void equiparItem(Personagem p) {
        Mochila mochila = p.getInventario();
        List<Item> itens = mochila.getItens();
        
        // Filtra equipamentos na mochila
        List<Equipamento> equipsMochila = new ArrayList<>();
        for (Item item : itens) {
            if (item instanceof Equipamento) {
                equipsMochila.add((Equipamento) item);
            }
        }

        if (equipsMochila.isEmpty()) {
            System.out.println("Nenhum equipamento disponível na mochila para equipar.");
            return;
        }

        System.out.println("\nSelecione o equipamento a vestir:");
        for (int i = 0; i < equipsMochila.size(); i++) {
            Equipamento eq = equipsMochila.get(i);
            System.out.printf("%d. %s (Slot: %s, Modificadores: %s)\n", i + 1, eq.getNome(), eq.getSlotCompativel(), eq.getModificadoresStatus());
        }
        System.out.println("0. Voltar");
        int choice = InputUtil.readInt("Escolha (ou 0 para Voltar): ", 0, equipsMochila.size());
        if (choice == 0) return;
        Equipamento selecionado = equipsMochila.get(choice - 1);

        System.out.println("Selecione o Slot de destino:");
        SlotType[] slots = SlotType.values();
        for (int i = 0; i < slots.length; i++) {
            System.out.printf("%d. %s\n", i + 1, slots[i]);
        }
        System.out.println("0. Voltar");
        int choiceSlot = InputUtil.readInt("Slot (ou 0 para Cancelar): ", 0, slots.length);
        if (choiceSlot == 0) return;
        SlotType slotDestino = slots[choiceSlot - 1];

        try {
            // Remove o item da mochila
            InventoryManager.removerItem(mochila, selecionado);
            // Equipa o item (e recebe o antigo caso estivesse preenchido)
            List<Equipamento> desequipados = EquipmentManager.equipar(p, selecionado, slotDestino);
            // Devolve os desequipados para a mochila
            for (Equipamento des : desequipados) {
                InventoryManager.adicionarItem(mochila, des);
                System.out.println("Item desequipado retornado para a mochila: " + des.getNome());
            }
            repository.salvar(p);
            System.out.println("Equipamento '" + selecionado.getNome() + "' vestido no slot " + slotDestino + " com sucesso!");
        } catch (Exception e) {
            // Devolve à mochila em caso de erro de validação
            if (!mochila.getItens().contains(selecionado)) {
                mochila.getItens().add(selecionado);
            }
            System.out.println("Erro ao equipar: " + e.getMessage());
        }
    }

    private void desequiparItem(Personagem p) {
        System.out.println("\nEscolha qual slot desequipar:");
        List<SlotType> slotsComItens = new ArrayList<>();
        p.getEquipamentosEquipados().forEach((slot, eq) -> {
            if (eq != null) {
                slotsComItens.add(slot);
            }
        });

        if (slotsComItens.isEmpty()) {
            System.out.println("Nenhum item equipado nos slots no momento.");
            return;
        }

        for (int i = 0; i < slotsComItens.size(); i++) {
            SlotType slot = slotsComItens.get(i);
            Equipamento eq = p.getEquipamentosEquipados().get(slot);
            System.out.printf("%d. Slot %s: %s\n", i + 1, slot, eq.getNome());
        }
        System.out.println("0. Voltar");

        int choice = InputUtil.readInt("Opção (ou 0 para Voltar): ", 0, slotsComItens.size());
        if (choice == 0) return;
        SlotType slotSelecionado = slotsComItens.get(choice - 1);

        try {
            Equipamento des = EquipmentManager.desequipar(p, slotSelecionado);
            if (des != null) {
                InventoryManager.adicionarItem(p.getInventario(), des);
                repository.salvar(p);
                System.out.println("Desequipado: " + des.getNome() + " movido para a mochila.");
            }
        } catch (MochilaCheiaException e) {
            System.out.println("Erro: Não há espaço na mochila para guardar o item desequipado!");
        }
    }

    private void editarItem(Personagem p) {
        List<ItemRef> refs = obterTodosItensDoPersonagem(p);
        if (refs.isEmpty()) {
            System.out.println("O personagem não possui nenhum item (mochila e slots vazios).");
            return;
        }

        System.out.println("\n--- EDICÃO DE ITENS ---");
        for (int i = 0; i < refs.size(); i++) {
            ItemRef ref = refs.get(i);
            String local = ref.isEquipado() ? "Equipado no slot " + ref.slotEquipado : "Na Mochila";
            System.out.printf("%d. %s (%s) - %s\n", i + 1, ref.item.getNome(), ref.item.getDescricao(), local);
        }
        int choice = InputUtil.readInt("Selecione o item para editar: ", 1, refs.size());
        Item selecionado = refs.get(choice - 1).item;

        System.out.println("\nEditando item: " + selecionado.getNome());
        String novoNome = InputUtil.readString("Novo Nome (deixe vazio para manter): ");
        if (!novoNome.trim().isEmpty()) selecionado.setNome(novoNome);

        String novaDesc = InputUtil.readString("Nova Descrição (deixe vazio para manter): ");
        if (!novaDesc.trim().isEmpty()) selecionado.setDescricao(novaDesc);

        Double novoValor = InputUtil.readOptionalDouble("Novo Valor");
        if (novoValor != null) {
            selecionado.setValorComercial(novoValor);
            selecionado.setTipoMoeda("G$");
        }

        if (selecionado instanceof Equipamento) {
            Equipamento equip = (Equipamento) selecionado;
            
            if (InputUtil.readBoolean("Deseja redefinir os modificadores de status? (s/n): ")) {
                equip.getModificadoresStatus().clear();
                while (InputUtil.readBoolean("Adicionar modificador de atributo? (s/n): ")) {
                    String attr = InputUtil.readStringNotEmpty("Nome do atributo (ex: Vida, Forca): ");
                    double mod = InputUtil.readDouble("Modificador (+/- valor): ");
                    equip.getModificadoresStatus().put(attr, mod);
                }
            }

            if (InputUtil.readBoolean("Deseja redefinir as habilidades embutidas? (s/n): ")) {
                equip.getHabilidadesEmbutidas().clear();
                while (InputUtil.readBoolean("Adicionar habilidade embutida? (s/n): ")) {
                    String habNome = InputUtil.readStringNotEmpty("Nome da Habilidade: ");
                    String habTipo = InputUtil.readBoolean("Tipo da Habilidade: Ativa (s) ou Passiva (n)? ") ? "ATIVA" : "PASSIVA";
                    String habDesc = InputUtil.readString("Descrição da habilidade: ");
                    equip.getHabilidadesEmbutidas().add(new Habilidade(habNome, habTipo, habDesc));
                }
            }
        } else if (selecionado instanceof ItemConsumivel) {
            ItemConsumivel cons = (ItemConsumivel) selecionado;
            Integer cargas = InputUtil.readOptionalInt("Quantidade de Cargas");
            if (cargas != null) {
                cons.setQuantidadeCargas(cargas);
            }
        }

        StatusManager.recalcularStatus(p);
        repository.salvar(p);
        System.out.println("Item editado com sucesso!");
    }

    private void deletarItem(Personagem p) {
        List<ItemRef> refs = obterTodosItensDoPersonagem(p);
        if (refs.isEmpty()) {
            System.out.println("O personagem não possui nenhum item para excluir.");
            return;
        }

        System.out.println("\n--- EXCLUIR ITEM ---");
        for (int i = 0; i < refs.size(); i++) {
            ItemRef ref = refs.get(i);
            String local = ref.isEquipado() ? "Equipado no slot " + ref.slotEquipado : "Na Mochila";
            System.out.printf("%d. %s - %s\n", i + 1, ref.item.getNome(), local);
        }
        int choice = InputUtil.readInt("Selecione o item para deletar: ", 1, refs.size());
        ItemRef refSelecionada = refs.get(choice - 1);

        boolean conf = InputUtil.readBoolean("Tem certeza de que deseja deletar permanentemente o item " + refSelecionada.item.getNome() + "? (s/n): ");
        if (!conf) {
            System.out.println("Cancelado.");
            return;
        }

        if (refSelecionada.isEquipado()) {
            EquipmentManager.desequipar(p, refSelecionada.slotEquipado);
        } else {
            p.getInventario().getItens().remove(refSelecionada.item);
        }

        StatusManager.recalcularStatus(p);
        repository.salvar(p);
        System.out.println("Item deletado com sucesso!");
    }

    private List<ItemRef> obterTodosItensDoPersonagem(Personagem p) {
        List<ItemRef> refs = new ArrayList<>();
        for (int i = 0; i < p.getInventario().getItens().size(); i++) {
            refs.add(new ItemRef(p.getInventario().getItens().get(i), null));
        }
        p.getEquipamentosEquipados().forEach((slot, eq) -> {
            if (eq != null) {
                boolean jaExiste = refs.stream().anyMatch(r -> r.item == eq);
                if (!jaExiste) {
                    refs.add(new ItemRef(eq, slot));
                }
            }
        });
        return refs;
    }

    private static class ItemRef {
        Item item;
        SlotType slotEquipado;

        ItemRef(Item item, SlotType slotEquipado) {
            this.item = item;
            this.slotEquipado = slotEquipado;
        }

        boolean isEquipado() {
            return slotEquipado != null;
        }
    }

    private void gerenciarCarteira(Personagem p) {
        Mochila mochila = p.getInventario();
        System.out.println("\n--- GERENCIAR CARTEIRA ---");
        System.out.println("Saldo Atual: G$ " + mochila.getDinheiroG() + " / " + mochila.getMaxG() + " (Teto)");
        System.out.println("1. Adicionar G$ (com verificação)");
        System.out.println("2. Gastar G$");
        System.out.println("0. Voltar");
        int opcao = InputUtil.readInt("Opção (ou 0 para Voltar): ", 0, 2);
        if (opcao == 0) return;

        double valor = InputUtil.readDouble("Valor: ", 0, Double.MAX_VALUE);

        switch (opcao) {
            case 1:
                boolean erro = InputUtil.readBoolean("Lançar erro se ultrapassar limite de G$ " + mochila.getMaxG() + "? (s/n): ");
                try {
                    InventoryManager.adicionarG(mochila, valor, erro);
                    System.out.println("Carteira de G$ atualizada. Saldo atual: G$ " + mochila.getDinheiroG());
                } catch (CarteiraCheiaException e) {
                    System.out.println("Erro: " + e.getMessage());
                }
                break;
            case 2:
                if (InventoryManager.gastarG(mochila, valor)) {
                    System.out.println("Gasto G$ " + valor + " com sucesso.");
                } else {
                    System.out.println("Erro: Saldo G$ insuficiente!");
                }
                break;
        }
        repository.salvar(p);
    }

    // ==========================================
    // MENU DE MAGIAS E COMBATE (ARENA)
    // ==========================================
    private void menuMagiasCombate() {
        while (true) {
            MenuBuilder menu = new MenuBuilder("✨ MAGIAS E CONHECIMENTO ✨");
            menu.addItem(1, "Ensinar Magia", "Aprender novo poder")
                .addItem(2, "Esquecer Magia", "Desaprender poder")
                .addItem(0, "Voltar", "Menu anterior");

            int opcao = menu.getUserChoice(0, 2);

            switch (opcao) {
                case 1:
                    ensinarMagia();
                    break;
                case 2:
                    esquecerMagia();
                    break;
                case 0:
                    return;
            }
        }
    }

    private void ensinarMagia() {
        Personagem p = selecionarPersonagem();
        if (p == null) return;

        if (magias.isEmpty()) {
            UiFormatter.printWarning("Nenhuma magia disponível nas tabelas globais.");
            return;
        }

        System.out.println("Selecione a Magia para Ensinar:");
        for (int i = 0; i < magias.size(); i++) {
            Magia m = magias.get(i);
            System.out.printf("%d. %s (%s)\n", i + 1, m.getNome(), m.getNomeTraduzido());
        }
        System.out.println("0. Voltar");
        int index = InputUtil.readInt("Magia (ou 0 para Voltar): ", 0, magias.size());
        if (index == 0) return;
        Magia magiaEscolhida = magias.get(index - 1);

        // Verifica duplicidade
        boolean jaSabe = p.getMagias().stream().anyMatch(m -> m.getNome().equalsIgnoreCase(magiaEscolhida.getNome()));
        if (jaSabe) {
            UiFormatter.printWarning(p.getNome() + " já domina a " + magiaEscolhida.getNome());
            return;
        }

        p.getMagias().add(magiaEscolhida);
        StatusManager.recalcularStatus(p);
        repository.salvar(p);
        UiFormatter.printSuccess(p.getNome() + " aprendeu " + magiaEscolhida.getNome() + "! ✨");
    }

    private void esquecerMagia() {
        Personagem p = selecionarPersonagem();
        if (p == null) return;

        if (p.getMagias().isEmpty()) {
            UiFormatter.printWarning(p.getNome() + " não conhece nenhuma magia no momento.");
            return;
        }

        System.out.println("Selecione qual magia esquecer:");
        for (int i = 0; i < p.getMagias().size(); i++) {
            System.out.printf("%d. %s\n", i + 1, p.getMagias().get(i).getNome());
        }
        System.out.println("0. Voltar");
        int indexOpt = InputUtil.readInt("Opção (ou 0 para Voltar): ", 0, p.getMagias().size());
        if (indexOpt == 0) return;
        Magia removida = p.getMagias().remove(indexOpt - 1);

        StatusManager.recalcularStatus(p);
        repository.salvar(p);
        UiFormatter.printSuccess(p.getNome() + " esqueceu a magia " + removida.getNome() + ". Status recalculados! 🗑️");
    }

    private void carregarMonstros() {
        File file = new File("config/monstros.json");
        ObjectMapper mapper = new ObjectMapper();
        if (!file.exists()) {
            List<Monstro> defaults = new ArrayList<>();
            
            // Goblin
            List<LootEntry> entriesGoblin = new ArrayList<>();
            entriesGoblin.add(new LootEntry(0.70, "MOEDA", "", 15.0));
            entriesGoblin.add(new LootEntry(0.30, "ITEM", "Adaga de Cobre", 0.0));
            LootTable tableGoblin = new LootTable(entriesGoblin);
            Monstro goblin = new Monstro("Goblin Saqueador", "Orcoide", Arrays.asList("Ataque Furtivo", "Arremesso de Pedra"), tableGoblin);
            goblin.getStatusFinal().put("vida", 70.0);
            goblin.getStatusFinal().put("forca", 6.0);
            goblin.getStatusFinal().put("velocidade", 14.0);
            goblin.getStatusFinal().put("destreza", 8.0);
            defaults.add(goblin);

            // Dragão
            List<LootEntry> entriesDragon = new ArrayList<>();
            entriesDragon.add(new LootEntry(1.00, "MOEDA", "", 250.0));
            entriesDragon.add(new LootEntry(0.50, "ITEM", "Elmo de Ferro", 0.0));
            LootTable tableDragon = new LootTable(entriesDragon);
            Monstro dragon = new Monstro("Dragão Jovem", "Réptil Alado", Arrays.asList("Sopro de Fogo", "Mordida Feroz"), tableDragon);
            dragon.getStatusFinal().put("vida", 400.0);
            dragon.getStatusFinal().put("forca", 28.0);
            dragon.getStatusFinal().put("velocidade", 16.0);
            dragon.getStatusFinal().put("destreza", 10.0);
            defaults.add(dragon);

            try {
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                mapper.writerWithDefaultPrettyPrinter().writeValue(file, defaults);
                monstros = defaults;
            } catch (IOException e) {
                System.err.println("Erro ao salvar monstros padrão: " + e.getMessage());
            }
        } else {
            try {
                monstros = mapper.readValue(file, new com.fasterxml.jackson.core.type.TypeReference<List<Monstro>>() {});
            } catch (IOException e) {
                System.err.println("Erro ao carregar monstros: " + e.getMessage());
                monstros = new ArrayList<>();
            }
        }
    }

    private Personagem clonarPersonagem(Personagem original) {
        if (original == null) return null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            String json = mapper.writeValueAsString(original);
            return mapper.readValue(json, Personagem.class);
        } catch (Exception e) {
            System.err.println("Erro ao clonar personagem: " + e.getMessage());
            return original;
        }
    }

    private boolean consumirMunicaoQuantidade(Mochila mochila, String nomeItem, int quantidadeNecessaria) {
        if (mochila == null || mochila.getItens() == null || nomeItem == null || quantidadeNecessaria <= 0) {
            return false;
        }

        // Primeiro conta a quantidade total disponível
        int totalDisponivel = 0;
        for (Item item : mochila.getItens()) {
            if (item.getNome().equalsIgnoreCase(nomeItem)) {
                if (item instanceof ItemConsumivel) {
                    totalDisponivel += ((ItemConsumivel) item).getQuantidadeCargas();
                } else {
                    totalDisponivel += 1;
                }
            }
        }

        if (totalDisponivel < quantidadeNecessaria) {
            return false;
        }

        // Agora consome
        int restanteParaConsumir = quantidadeNecessaria;
        List<Item> itensParaRemover = new ArrayList<>();

        for (Item item : mochila.getItens()) {
            if (item.getNome().equalsIgnoreCase(nomeItem)) {
                if (item instanceof ItemConsumivel) {
                    ItemConsumivel c = (ItemConsumivel) item;
                    int cargasDisponiveis = c.getQuantidadeCargas();
                    if (cargasDisponiveis >= restanteParaConsumir) {
                        c.setQuantidadeCargas(cargasDisponiveis - restanteParaConsumir);
                        restanteParaConsumir = 0;
                        if (c.getQuantidadeCargas() <= 0) {
                            itensParaRemover.add(c);
                        }
                        break;
                    } else {
                        restanteParaConsumir -= cargasDisponiveis;
                        c.setQuantidadeCargas(0);
                        itensParaRemover.add(c);
                    }
                } else {
                    restanteParaConsumir -= 1;
                    itensParaRemover.add(item);
                    if (restanteParaConsumir == 0) {
                        break;
                    }
                }
            }
        }

        mochila.getItens().removeAll(itensParaRemover);
        return true;
    }



    private void menuImportacaoItens() {
        while (true) {
            MenuBuilder menu = new MenuBuilder("🛠️ IMPORTAÇÃO & CADASTRO DE DADOS 🛠️");
            menu.addItem(1, "Criar Item e Adicionar ao Catálogo", "Formulário manual")
                .addItem(2, "Importar Itens em Lote (CSV)", "Ler arquivos da pasta dados/imports")
                .addItem(3, "Importar Ataques em Lote (CSV)", "Ler arquivos da pasta dados/imports")
                .addItem(4, "Importar Passivas em Lote (CSV)", "Ler arquivos da pasta dados/imports")
                .addItem(5, "Gerenciar Catálogo", "Visualizar, editar ou excluir itens do catálogo")
                .addItem(6, "Visualizar Todos os Itens (Detalhado)", "Exibir detalhes de todos os itens cadastrados")
                .addItem(0, "Voltar", "Menu anterior");

            int opcao = menu.getUserChoice(0, 6);
            switch (opcao) {
                case 1:
                    criarItemManualCatalogo();
                    break;
                case 2:
                    importarLoteCSV();
                    break;
                case 3:
                    importarAtaquesCSV();
                    break;
                case 4:
                    importarPassivasCSV();
                    break;
                case 5:
                    gerenciarCatalogoItens();
                    break;
                case 6:
                    visualizarTodosItensDetalhado();
                    break;
                case 0:
                    return;
            }
        }
    }

    private void criarItemManualCatalogo() {
        UiFormatter.printSubtitle("CADASTRAR NOVO ITEM NO CATÁLOGO");
        String nome = InputUtil.readStringNotEmpty("Nome do Item: ");
        String desc = InputUtil.readString("Descrição: ");

        System.out.println("Selecione a Raridade:");
        Raridade[] raridades = Raridade.values();
        for (int i = 0; i < raridades.length; i++) {
            System.out.printf("%d. %s (Multiplicador: x%.1f)\n", i + 1, raridades[i], raridades[i].getMultiplicador());
        }
        int idxR = InputUtil.readInt("Opção: ", 1, raridades.length);
        Raridade raridade = raridades[idxR - 1];

        System.out.println("\nTipo do Item:");
        System.out.println("1. Equipamento");
        System.out.println("2. Consumível");
        System.out.println("0. Cancelar");
        int tipoItem = InputUtil.readInt("Opção: ", 0, 2);
        if (tipoItem == 0) return;

        Item novoItem;
        if (tipoItem == 1) {
            System.out.println("Selecione o Slot Compatível:");
            SlotType[] slots = SlotType.values();
            for (int i = 0; i < slots.length; i++) {
                System.out.printf("%d. %s\n", i + 1, slots[i]);
            }
            System.out.println("0. Cancelar");
            int idxSlot = InputUtil.readInt("Opção: ", 0, slots.length);
            if (idxSlot == 0) return;
            SlotType slot = slots[idxSlot - 1];

            Map<String, Double> mods = new HashMap<>();
            while (InputUtil.readBoolean("Adicionar modificador de atributo? (s/n): ")) {
                String attr = InputUtil.readStringNotEmpty("Nome do atributo (ex: Vida, Forca): ");
                double mod = InputUtil.readDouble("Modificador (+/- valor): ");
                mods.put(attr, mod);
            }

            List<Habilidade> habs = new ArrayList<>();
            while (InputUtil.readBoolean("Adicionar habilidade embutida? (s/n): ")) {
                String habNome = InputUtil.readStringNotEmpty("Nome da Habilidade: ");
                String habTipo = InputUtil.readBoolean("Habilidade Ativa (s) ou Passiva (n)? ") ? "ATIVA" : "PASSIVA";
                String habDesc = InputUtil.readString("Descrição da habilidade: ");
                habs.add(new Habilidade(habNome, habTipo, habDesc));
            }

            novoItem = new Equipamento(nome, desc, 0.0, "G$", slot, mods, habs);
        } else {
            int cargas = InputUtil.readInt("Cargas / Usos: ", 1, 100);
            novoItem = new ItemConsumivel(nome, desc, 0.0, "G$", cargas);
        }

        novoItem.setRaridade(raridade);
        int poder = ItemPowerCalculator.calcularPoder(novoItem);
        novoItem.setPoder(poder);
        double precoSugerido = PricingEngine.precoBaseSugerido(novoItem);

        System.out.println("\n----------------------------------------------");
        System.out.printf("📊 PODER ESTIMADO: %d | RARIDADE: %s\n", poder, raridade);
        System.out.printf("💰 PREÇO BASE SUGERIDO: G$ %.2f\n", precoSugerido);
        System.out.println("----------------------------------------------");
        
        double valor = InputUtil.readDouble("Valor Comercial Final (digite 0 para aceitar o preço sugerido): ", 0, Double.MAX_VALUE);
        if (valor <= 0.0) {
            valor = precoSugerido;
        }
        novoItem.setValorComercial(valor);

        ItemFactory.adicionarAoCatalogo(novoItem);
        UiFormatter.printSuccess("Item '" + nome + "' cadastrado no catálogo global!");
    }

    private void importarLoteCSV() {
        File dir = new File("dados/imports");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File[] csvFiles = dir.listFiles((d, name) -> name.endsWith(".csv"));
        if (csvFiles == null || csvFiles.length == 0) {
            UiFormatter.printWarning("Nenhum arquivo .csv encontrado na pasta '/dados/imports/'.");
            System.out.println("Crie um arquivo .csv contendo linhas formatadas como:");
            System.out.println("Nome;Descrição;PreçoBase;EQUIPAMENTO;Slot;Modificadores(Atributo:Valor,Atributo:Valor);Habilidades(Nome:Tipo:Desc)");
            System.out.println("Exemplo: Espada de Ferro;Espada Curta;80.0;EQUIPAMENTO;MAO_PRINCIPAL;Forca:2;Golpe Rapido:ATIVA:Dano leve");
            InputUtil.pressEnterToContinue();
            return;
        }

        UiFormatter.printSubtitle("SELECIONE O ARQUIVO PARA IMPORTAR:");
        for (int i = 0; i < csvFiles.length; i++) {
            System.out.printf("%d. %s\n", i + 1, csvFiles[i].getName());
        }
        System.out.println("0. Voltar");
        int choice = InputUtil.readInt("Opção: ", 0, csvFiles.length);
        if (choice == 0) return;

        File selected = csvFiles[choice - 1];
        try {
            int count = ItemFactory.importarCSV(selected);
            UiFormatter.printSuccess("Importação concluída! " + count + " itens foram adicionados ao catálogo.");
        } catch (Exception e) {
            UiFormatter.printError("Erro ao importar CSV: " + e.getMessage());
        }
        InputUtil.pressEnterToContinue();
    }

    private void importarAtaquesCSV() {
        File dir = new File("dados/imports");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File[] csvFiles = dir.listFiles((d, name) -> name.endsWith(".csv"));
        if (csvFiles == null || csvFiles.length == 0) {
            UiFormatter.printWarning("Nenhum arquivo .csv encontrado na pasta '/dados/imports/'.");
            System.out.println("Crie um arquivo .csv contendo linhas formatadas como:");
            System.out.println("Nome;Descrição;TipoMagiaOuArma;Dano;Nível");
            System.out.println("Exemplo: Bola de Fogo;Dispara uma bola de fogo;Pyromacia;2d10;1");
            InputUtil.pressEnterToContinue();
            return;
        }

        UiFormatter.printSubtitle("IMPORTAR ATAQUES: SELECIONE O ARQUIVO:");
        for (int i = 0; i < csvFiles.length; i++) {
            System.out.printf("%d. %s\n", i + 1, csvFiles[i].getName());
        }
        System.out.println("0. Voltar");
        int choice = InputUtil.readInt("Opção: ", 0, csvFiles.length);
        if (choice == 0) return;

        File selected = csvFiles[choice - 1];
        try {
            int count = AtaqueFactory.importarCSV(selected);
            UiFormatter.printSuccess("Importação concluída! " + count + " ataques foram adicionados ao catálogo.");
        } catch (Exception e) {
            UiFormatter.printError("Erro ao importar CSV: " + e.getMessage());
        }
        InputUtil.pressEnterToContinue();
    }

    private void importarPassivasCSV() {
        File dir = new File("dados/imports");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File[] csvFiles = dir.listFiles((d, name) -> name.endsWith(".csv"));
        if (csvFiles == null || csvFiles.length == 0) {
            UiFormatter.printWarning("Nenhum arquivo .csv encontrado na pasta '/dados/imports/'.");
            System.out.println("Crie um arquivo .csv contendo linhas formatadas como:");
            System.out.println("Nome;Descrição;TipoMagiaOuArma;Condição;Efeito");
            System.out.println("Exemplo: Parada Temporal;Para o tempo quando prestes a morrer;Cronomacia;preste a receber um ataque fatal;paro o tempo");
            InputUtil.pressEnterToContinue();
            return;
        }

        UiFormatter.printSubtitle("IMPORTAR PASSIVAS: SELECIONE O ARQUIVO:");
        for (int i = 0; i < csvFiles.length; i++) {
            System.out.printf("%d. %s\n", i + 1, csvFiles[i].getName());
        }
        System.out.println("0. Voltar");
        int choice = InputUtil.readInt("Opção: ", 0, csvFiles.length);
        if (choice == 0) return;

        File selected = csvFiles[choice - 1];
        try {
            int count = PassivaFactory.importarCSV(selected);
            UiFormatter.printSuccess("Importação concluída! " + count + " passivas foram adicionados ao catálogo.");
        } catch (Exception e) {
            UiFormatter.printError("Erro ao importar CSV: " + e.getMessage());
        }
        InputUtil.pressEnterToContinue();
    }

    // ==========================================
    // MENU DE CONFIGURAÇÕES / TABELAS GLOBAIS
    // ==========================================
    private void menuConfiguracoes() {
        while (true) {
            MenuBuilder menu = new MenuBuilder("⚙️ TABELAS TÁTICAS GERAIS ⚙️");
            menu.addItem(1, "Ver Raças", "Raças disponíveis")
                .addItem(2, "Ver Classes", "Classes disponíveis")
                .addItem(3, "Ver Magias", "Magias e poderes")
                .addItem(0, "Voltar", "Menu anterior");

            int opcao = menu.getUserChoice(0, 3);

            switch (opcao) {
                case 1:
                    exibirRacas();
                    InputUtil.pressEnterToContinue();
                    break;
                case 2:
                    exibirClasses();
                    InputUtil.pressEnterToContinue();
                    break;
                case 3:
                    exibirMagias();
                    InputUtil.pressEnterToContinue();
                    break;
                case 0:
                    return;
            }
        }
    }

    private void exibirRacas() {
        UiFormatter.printTitle("📚 RAÇAS REGISTRADAS");
        if (racas.isEmpty()) {
            UiFormatter.printWarning("Nenhuma raça cadastrada.");
            return;
        }
        for (int i = 0; i < racas.size(); i++) {
            Raca r = racas.get(i);
            System.out.println("\n" + (i + 1) + ". " + UiFormatter.BOLD + UiFormatter.YELLOW + r.getRaca() + UiFormatter.RESET);
            
            if (!r.getVantagens().getModificadores().isEmpty() || !r.getVantagens().getEscritas().isEmpty()) {
                System.out.println("  " + UiFormatter.GREEN + "✓ Vantagens:" + UiFormatter.RESET);
                r.getVantagens().getModificadores().forEach(m -> UiFormatter.printBullet(m));
                r.getVantagens().getEscritas().forEach(e -> System.out.println("    [Observação] " + e));
            }
            
            if (!r.getDesvantagens().getModificadores().isEmpty() || !r.getDesvantagens().getEscritas().isEmpty()) {
                System.out.println("  " + UiFormatter.RED + "✗ Desvantagens:" + UiFormatter.RESET);
                r.getDesvantagens().getModificadores().forEach(m -> UiFormatter.printBullet(m));
                r.getDesvantagens().getEscritas().forEach(e -> System.out.println("    [Observação] " + e));
            }
        }
    }

    private void exibirClasses() {
        UiFormatter.printTitle("⚔️ CLASSES REGISTRADAS");
        if (classes.isEmpty()) {
            UiFormatter.printWarning("Nenhuma classe cadastrada.");
            return;
        }
        for (int i = 0; i < classes.size(); i++) {
            Classe c = classes.get(i);
            System.out.println("\n" + (i + 1) + ". " + UiFormatter.BOLD + UiFormatter.YELLOW + c.getClasse() + UiFormatter.RESET);
            UiFormatter.printItem("Especialidade", c.getEspecialidades());
            UiFormatter.printItem("Mochila", c.getTamanhoDaMochila());
            
            if (!c.getBalanceamentoDeStatus().getPositivos().isEmpty()) {
                System.out.println("  " + UiFormatter.GREEN + "✓ Modificadores Positivos:" + UiFormatter.RESET);
                c.getBalanceamentoDeStatus().getPositivos().forEach(m -> UiFormatter.printBullet(m));
            }
            
            if (!c.getBalanceamentoDeStatus().getNegativos().isEmpty()) {
                System.out.println("  " + UiFormatter.RED + "✗ Modificadores Negativos:" + UiFormatter.RESET);
                c.getBalanceamentoDeStatus().getNegativos().forEach(m -> UiFormatter.printBullet(m));
            }
        }
    }

    private void exibirMagias() {
        UiFormatter.printTitle("✨ MAGIAS REGISTRADAS");
        if (magias.isEmpty()) {
            UiFormatter.printWarning("Nenhuma magia cadastrada.");
            return;
        }
        for (int i = 0; i < magias.size(); i++) {
            Magia m = magias.get(i);
            System.out.println("\n" + (i + 1) + ". " + UiFormatter.BOLD + UiFormatter.YELLOW + m.getNome() + 
                             UiFormatter.RESET + " (" + UiFormatter.BLUE + m.getNomeTraduzido() + UiFormatter.RESET + ")");
            
            if (m.getObservacao() != null && !m.getObservacao().isEmpty()) {
                System.out.println("  " + UiFormatter.CYAN + "Observação: " + m.getObservacao() + UiFormatter.RESET);
            }
            
            if (!m.getModificadores().getPositivos().isEmpty()) {
                System.out.println("  " + UiFormatter.GREEN + "✓ Efeitos Positivos:" + UiFormatter.RESET);
                m.getModificadores().getPositivos().forEach(mod -> UiFormatter.printBullet(mod));
            }
            
            if (!m.getModificadores().getNegativos().isEmpty()) {
                System.out.println("  " + UiFormatter.RED + "✗ Efeitos Negativos:" + UiFormatter.RESET);
                m.getModificadores().getNegativos().forEach(mod -> UiFormatter.printBullet(mod));
            }
        }
    }

    private void gerenciarCatalogoItens() {
        while (true) {
            List<Item> catalogo = ItemFactory.listarCatalogo();
            if (catalogo.isEmpty()) {
                UiFormatter.printWarning("O catálogo está vazio!");
                InputUtil.pressEnterToContinue();
                return;
            }

            UiFormatter.printSubtitle("GERENCIAR CATÁLOGO DE ITENS 📦");
            for (int i = 0; i < catalogo.size(); i++) {
                Item item = catalogo.get(i);
                String tipo = (item instanceof Equipamento) ? "EQUIPAMENTO" : "CONSUMÍVEL";
                System.out.printf("%d. %s [%s] - G$ %.2f\n", i + 1, item.getNome(), tipo, item.getValorComercial());
            }
            System.out.println("0. Voltar");

            int idx = InputUtil.readInt("Selecione um item (ou 0 para Voltar): ", 0, catalogo.size());
            if (idx == 0) return;

            Item itemSelecionado = catalogo.get(idx - 1);
            menuAcoesItem(itemSelecionado);
        }
    }

    private void menuAcoesItem(Item item) {
        while (true) {
            UiFormatter.printSubtitle("DETALHES DO ITEM: " + item.getNome().toUpperCase());
            System.out.printf("Nome: %s\n", item.getNome());
            System.out.printf("Descrição: %s\n", item.getDescricao());
            System.out.printf("Valor Comercial: %.2f %s\n", item.getValorComercial(), item.getTipoMoeda());
            
            if (item instanceof Equipamento) {
                Equipamento eq = (Equipamento) item;
                System.out.printf("Tipo: Equipamento (%s)\n", eq.getSlotCompativel());
                System.out.printf("Dano: %.1f\n", eq.getDano());
                System.out.printf("Munição/Recurso: %s (Qtd: %d)\n", 
                    eq.getTipoMunicao() != null ? eq.getTipoMunicao() : "Nenhuma", eq.getQuantidadeMunicao());
                System.out.printf("Modificadores: %s\n", eq.getModificadoresStatus());
                if (!eq.getHabilidadesEmbutidas().isEmpty()) {
                    System.out.println("Habilidades Embutidas:");
                    for (Habilidade hab : eq.getHabilidadesEmbutidas()) {
                        System.out.printf("  - %s (%s): %s\n", hab.getNome(), hab.getTipo(), hab.getDescricao());
                    }
                }
            } else if (item instanceof ItemConsumivel) {
                ItemConsumivel con = (ItemConsumivel) item;
                System.out.printf("Tipo: Consumível (%d Cargas)\n", con.getQuantidadeCargas());
            }
            
            System.out.println("\nAções:");
            System.out.println("1. Editar Item");
            System.out.println("2. Excluir Item do Catálogo");
            System.out.println("0. Voltar");
            
            int escolha = InputUtil.readInt("Opção: ", 0, 2);
            if (escolha == 0) return;
            
            if (escolha == 1) {
                editarItemCatalogo(item);
            } else if (escolha == 2) {
                if (InputUtil.readBoolean("Tem certeza de que deseja deletar este item permanentemente? (s/n): ")) {
                    ItemFactory.removerDoCatalogo(item.getNome());
                    UiFormatter.printSuccess("Item excluído do catálogo! 🗑️");
                    return;
                }
            }
        }
    }

    private void editarItemCatalogo(Item item) {
        UiFormatter.printSubtitle("EDITAR ITEM: " + item.getNome().toUpperCase());
        String nomeAntigo = item.getNome();
        
        String novoNome = InputUtil.readString("Novo Nome (deixe em branco para manter '" + item.getNome() + "'): ");
        if (novoNome != null && !novoNome.trim().isEmpty()) {
            item.setNome(novoNome.trim());
        }
        
        String novaDesc = InputUtil.readString("Nova Descrição (deixe em branco para manter '" + item.getDescricao() + "'): ");
        if (novaDesc != null && !novaDesc.trim().isEmpty()) {
            item.setDescricao(novaDesc.trim());
        }
        
        double novoValor = InputUtil.readDouble("Novo Valor Comercial (ou -1 para manter " + item.getValorComercial() + "): ", -1, Double.MAX_VALUE);
        if (novoValor >= 0) {
            item.setValorComercial(novoValor);
        }
        
        if (item instanceof Equipamento) {
            Equipamento eq = (Equipamento) item;
            
            double novoDano = InputUtil.readDouble("Novo Dano da Arma (ou -1 para manter " + eq.getDano() + "): ", -1, Double.MAX_VALUE);
            if (novoDano >= 0) {
                eq.setDano(novoDano);
            }
            
            String novaMun = InputUtil.readString("Nova Munição (ou 'nenhuma' para limpar, em branco para manter '" + (eq.getTipoMunicao() != null ? eq.getTipoMunicao() : "") + "'): ");
            if (novaMun != null && !novaMun.trim().isEmpty()) {
                if (novaMun.equalsIgnoreCase("nenhuma")) {
                    eq.setTipoMunicao(null);
                } else {
                    eq.setTipoMunicao(novaMun.trim());
                }
            }
            
            int novaQtdMun = InputUtil.readInt("Nova Quantidade Munição (ou -1 para manter " + eq.getQuantidadeMunicao() + "): ", -1, Integer.MAX_VALUE);
            if (novaQtdMun >= 0) {
                eq.setQuantidadeMunicao(novaQtdMun);
            }
        } else if (item instanceof ItemConsumivel) {
            ItemConsumivel con = (ItemConsumivel) item;
            int novasCargas = InputUtil.readInt("Novas Cargas (ou -1 para manter " + con.getQuantidadeCargas() + "): ", -1, Integer.MAX_VALUE);
            if (novasCargas >= 0) {
                con.setQuantidadeCargas(novasCargas);
            }
        }
        
        ItemFactory.atualizarNoCatalogo(nomeAntigo, item);
        UiFormatter.printSuccess("Item updated successfully in catalog! 💾");
    }

    private void visualizarTodosItensDetalhado() {
        List<Item> catalogo = ItemFactory.listarCatalogo();
        if (catalogo.isEmpty()) {
            UiFormatter.printWarning("O catálogo está vazio!");
            InputUtil.pressEnterToContinue();
            return;
        }

        UiFormatter.printTitle("📦 DETALHES DE TODOS OS ITENS NO CATÁLOGO (" + catalogo.size() + ")");
        for (int i = 0; i < catalogo.size(); i++) {
            Item item = catalogo.get(i);
            System.out.printf("\n%d. %s\n", i + 1, UiFormatter.BOLD + item.getNome().toUpperCase() + UiFormatter.RESET);
            System.out.printf("   Descrição: %s\n", item.getDescricao());
            System.out.printf("   Valor Comercial: %.2f %s\n", item.getValorComercial(), item.getTipoMoeda());
            
            if (item instanceof Equipamento) {
                Equipamento eq = (Equipamento) item;
                System.out.printf("   Tipo: Equipamento (%s)\n", eq.getSlotCompativel());
                System.out.printf("   Dano: %.1f\n", eq.getDano());
                if (eq.getTipoMunicao() != null && !eq.getTipoMunicao().isEmpty()) {
                    System.out.printf("   Munição/Recurso: %s (Qtd consumida: %d)\n", eq.getTipoMunicao(), eq.getQuantidadeMunicao());
                }
                if (eq.getModificadoresStatus() != null && !eq.getModificadoresStatus().isEmpty()) {
                    System.out.printf("   Modificadores de Atributos: %s\n", eq.getModificadoresStatus());
                }
                if (!eq.getHabilidadesEmbutidas().isEmpty()) {
                    System.out.println("   Habilidades Embutidas:");
                    for (Habilidade hab : eq.getHabilidadesEmbutidas()) {
                        System.out.printf("     - %s (%s): %s\n", hab.getNome(), hab.getTipo(), hab.getDescricao());
                    }
                }
            } else if (item instanceof ItemConsumivel) {
                ItemConsumivel con = (ItemConsumivel) item;
                System.out.printf("   Tipo: Consumível (%d Cargas)\n", con.getQuantidadeCargas());
            }
            System.out.println("   " + "─".repeat(40));
        }
        InputUtil.pressEnterToContinue();
    }

    private void menuAtaques() {
        while (true) {
            MenuBuilder menu = new MenuBuilder("⚔️ GERENCIAR CATÁLOGO DE ATAQUES ⚔️");
            menu.addItem(1, "Criar Ataque Base", "Cadastrar novo ataque/habilidade inicial")
                .addItem(2, "Ver Todos os Ataques (Detalhado)", "Lista de ataques e suas árvores de evoluções")
                .addItem(3, "Gerenciar/Editar Ataques", "Editar, deletar ou associar evoluções")
                .addItem(0, "Voltar", "Menu anterior");

            int opcao = menu.getUserChoice(0, 3);
            switch (opcao) {
                case 1:
                    criarNovoAtaque();
                    break;
                case 2:
                    visualizarTodosAtaquesDetalhado();
                    break;
                case 3:
                    gerenciarAtaquesCrud();
                    break;
                case 0:
                    return;
            }
        }
    }

    private Ataque criarCamposAtaqueSemEvolucoes() {
        String nome = InputUtil.readStringNotEmpty("Nome do Ataque: ");
        String descricao = InputUtil.readString("Descrição: ");
        
        System.out.println("Tipo (Magia ou Estilo de Arma):");
        System.out.println("1. Cronomacia");
        System.out.println("2. Abstractomacia");
        System.out.println("3. Necromacia");
        System.out.println("4. Espectromacia");
        System.out.println("5. Armas de curta distância");
        System.out.println("6. Armas de longa distância");
        System.out.println("7. Outro (digitar personalizado)");
        int tOpt = InputUtil.readInt("Opção: ", 1, 7);
        String tipo = "";
        if (tOpt == 1) tipo = "Cronomacia";
        else if (tOpt == 2) tipo = "Abstractomacia";
        else if (tOpt == 3) tipo = "Necromacia";
        else if (tOpt == 4) tipo = "Espectromacia";
        else if (tOpt == 5) tipo = "Armas de curta distância";
        else if (tOpt == 6) tipo = "Armas de longa distância";
        else tipo = InputUtil.readStringNotEmpty("Digite o tipo: ");
        
        String dano = InputUtil.readStringNotEmpty("Dano (ex: 20d100, 3dforca, forcaD10): ");
        int nivel = InputUtil.readInt("Nível da Magia/Ataque (1 a 10): ", 1, 10);
        return new Ataque(nome, descricao, tipo, dano, new ArrayList<>(), new ArrayList<>(), nivel);
    }

    private void criarNovoAtaque() {
        UiFormatter.printSubtitle("CADASTRAR NOVO ATAQUE BASE");
        Ataque a = criarCamposAtaqueSemEvolucoes();
        if (InputUtil.readBoolean("Deseja cadastrar evoluções para este ataque agora? (s/n): ")) {
            adicionarEvolucoesAoAtaque(a);
        }
        AtaqueFactory.adicionarAoCatalogo(a);
        UiFormatter.printSuccess("Ataque '" + a.getNome() + "' cadastrado com sucesso no catálogo! ⚔️");
    }

    private void adicionarEvolucoesAoAtaque(Ataque ataqueBase) {
        while (true) {
            System.out.printf("\n--- Cadastrando evolução para o ataque '%s' ---\n", ataqueBase.getNome());
            System.out.println("1. Criar novo ataque e adicionar como evolução");
            System.out.println("2. Associar ataque já existente no catálogo como evolução");
            System.out.println("0. Finalizar cadastro de evoluções");
            int escolha = InputUtil.readInt("Opção: ", 0, 2);
            if (escolha == 0) break;
            
            Ataque evolucao = null;
            if (escolha == 1) {
                evolucao = criarCamposAtaqueSemEvolucoes();
            } else if (escolha == 2) {
                List<Ataque> catalogo = AtaqueFactory.listarCatalogo();
                if (catalogo.isEmpty()) {
                    System.out.println("O catálogo de ataques está vazio para associar!");
                    continue;
                }
                System.out.println("\nSelecione o ataque para associar:");
                for (int i = 0; i < catalogo.size(); i++) {
                    System.out.printf("%d. %s\n", i + 1, catalogo.get(i).getNome());
                }
                int idx = InputUtil.readInt("Opção: ", 1, catalogo.size());
                evolucao = AtaqueFactory.clonarAtaque(catalogo.get(idx - 1));
            }
            
            if (evolucao != null) {
                cadastrarRequisitosEvolucao(evolucao);
                ataqueBase.getEvolucoes().add(evolucao);
                System.out.printf("✅ Evolução '%s' adicionada a '%s'!\n", evolucao.getNome(), ataqueBase.getNome());
                
                if (InputUtil.readBoolean("Deseja cadastrar evoluções recursivas para esta evolução '" + evolucao.getNome() + "'? (s/n): ")) {
                    adicionarEvolucoesAoAtaque(evolucao);
                }
            }
        }
    }

    private void cadastrarRequisitosEvolucao(Ataque ataque) {
        System.out.printf("\n--- Cadastrar requisitos de evolução para '%s' ---\n", ataque.getNome());
        while (true) {
            System.out.println("1. Adicionar requisito de Proficiência (1-10)");
            System.out.println("2. Adicionar requisito de Ataque Prévio");
            System.out.println("3. Adicionar requisito de Tier");
            System.out.println("0. Finalizar requisitos");
            int opt = InputUtil.readInt("Opção: ", 0, 3);
            if (opt == 0) break;
            
            if (opt == 1) {
                System.out.println("Selecione a categoria:");
                System.out.println("1. Cronomacia");
                System.out.println("2. Abstractomacia");
                System.out.println("3. Necromacia");
                System.out.println("4. Espectromacia");
                System.out.println("5. Armas de curta distância");
                System.out.println("6. Armas de longa distância");
                System.out.println("7. Personalizada");
                int c = InputUtil.readInt("Opção: ", 1, 7);
                String cat = "";
                if (c == 1) cat = "Cronomacia";
                else if (c == 2) cat = "Abstractomacia";
                else if (c == 3) cat = "Necromacia";
                else if (c == 4) cat = "Espectromacia";
                else if (c == 5) cat = "Armas de curta distância";
                else if (c == 6) cat = "Armas de longa distância";
                else cat = InputUtil.readStringNotEmpty("Categoria: ");
                
                int nivel = InputUtil.readInt("Nível necessário (1 a 10): ", 1, 10);
                ataque.getRequisitosEvolucao().add(new RequisitoEvolucao("PROFICIENCIA", cat, nivel));
                System.out.println("Requisito de proficiência adicionado!");
            } else if (opt == 2) {
                String nomeAtq = InputUtil.readStringNotEmpty("Nome do ataque prévio necessário: ");
                ataque.getRequisitosEvolucao().add(new RequisitoEvolucao("ATAQUE_PREVIO", nomeAtq, 1));
                System.out.println("Requisito de ataque prévio adicionado!");
            } else if (opt == 3) {
                int tier = InputUtil.readInt("Tier necessário (1 a 10): ", 1, 10);
                ataque.getRequisitosEvolucao().add(new RequisitoEvolucao("TIER", "Tier", tier));
                System.out.println("Requisito de tier adicionado!");
            }
        }
    }

    private void visualizarTodosAtaquesDetalhado() {
        List<Ataque> catalogo = AtaqueFactory.listarCatalogo();
        if (catalogo.isEmpty()) {
            UiFormatter.printWarning("O catálogo de ataques está vazio!");
            InputUtil.pressEnterToContinue();
            return;
        }

        UiFormatter.printTitle("⚔️ ÁRVORE DE EVOLUÇÕES DE ATAQUES (" + catalogo.size() + ")");
        for (int i = 0; i < catalogo.size(); i++) {
            Ataque a = catalogo.get(i);
            System.out.printf("\n%d. %s [%s] (Dano: %s) [Nível: %d]\n", i + 1, UiFormatter.BOLD + a.getNome().toUpperCase() + UiFormatter.RESET, a.getTipoMagiaOuArma(), a.getDano(), a.getNivel());
            System.out.printf("   Descrição: %s\n", a.getDescricao());
            if (!a.getEvolucoes().isEmpty()) {
                System.out.println("   Evoluções:");
                for (Ataque sub : a.getEvolucoes()) {
                    exibirArvoreEvolucoes(sub, 2);
                }
            } else {
                System.out.println("   Sem evoluções cadastradas.");
            }
            System.out.println("   " + "─".repeat(40));
        }
        InputUtil.pressEnterToContinue();
    }

    private void exibirArvoreEvolucoes(Ataque a, int nivel) {
        String indent = "  ".repeat(nivel);
        System.out.printf("%s└─ %s (Dano: %s) [%s] (Nível: %d)\n", indent, UiFormatter.BOLD + a.getNome() + UiFormatter.RESET, a.getDano(), a.getTipoMagiaOuArma(), a.getNivel());
        if (a.getDescricao() != null && !a.getDescricao().isEmpty()) {
            System.out.printf("%s   Descrição: %s\n", indent, a.getDescricao());
        }
        if (!a.getRequisitosEvolucao().isEmpty()) {
            System.out.printf("%s   Requisitos:\n", indent);
            for (RequisitoEvolucao req : a.getRequisitosEvolucao()) {
                System.out.printf("%s     - %s\n", indent, req);
            }
        }
        for (Ataque sub : a.getEvolucoes()) {
            exibirArvoreEvolucoes(sub, nivel + 1);
        }
    }

    private void gerenciarAtaquesCrud() {
        while (true) {
            List<Ataque> catalogo = AtaqueFactory.listarCatalogo();
            if (catalogo.isEmpty()) {
                UiFormatter.printWarning("O catálogo de ataques está vazio!");
                InputUtil.pressEnterToContinue();
                return;
            }

            UiFormatter.printSubtitle("GERENCIAR CATÁLOGO DE ATAQUES ⚔️");
            for (int i = 0; i < catalogo.size(); i++) {
                Ataque a = catalogo.get(i);
                System.out.printf("%d. %s [%s] (Dano: %s) [Nível: %d]\n", i + 1, a.getNome(), a.getTipoMagiaOuArma(), a.getDano(), a.getNivel());
            }
            System.out.println("0. Voltar");

            int idx = InputUtil.readInt("Selecione um ataque (ou 0 para Voltar): ", 0, catalogo.size());
            if (idx == 0) return;

            Ataque ataqueSelecionado = catalogo.get(idx - 1);
            menuAcoesAtaque(ataqueSelecionado);
        }
    }

    private void menuAcoesAtaque(Ataque a) {
        while (true) {
            UiFormatter.printSubtitle("AÇÕES PARA O ATAQUE: " + a.getNome().toUpperCase());
            System.out.printf("Nome: %s\n", a.getNome());
            System.out.printf("Descrição: %s\n", a.getDescricao());
            System.out.printf("Tipo: %s\n", a.getTipoMagiaOuArma());
            System.out.printf("Dano: %s\n", a.getDano());
            System.out.printf("Nível: %d\n", a.getNivel());
            System.out.printf("Evoluções cadastradas: %d\n", a.getEvolucoes().size());

            System.out.println("\nOpções:");
            System.out.println("1. Editar Dados do Ataque");
            System.out.println("2. Adicionar/Gerenciar Evoluções deste Ataque");
            System.out.println("3. Deletar Ataque do Catálogo");
            System.out.println("0. Voltar");

            int escolha = InputUtil.readInt("Opção: ", 0, 3);
            if (escolha == 0) return;

            if (escolha == 1) {
                editarDadosAtaque(a);
            } else if (escolha == 2) {
                adicionarEvolucoesAoAtaque(a);
                AtaqueFactory.atualizarNoCatalogo(a.getNome(), a);
            } else if (escolha == 3) {
                if (InputUtil.readBoolean("Deseja realmente excluir o ataque '" + a.getNome() + "' permanentemente? (s/n): ")) {
                    AtaqueFactory.removerDoCatalogo(a.getNome());
                    UiFormatter.printSuccess("Ataque removido com sucesso!");
                    return;
                }
            }
        }
    }

    private void editarDadosAtaque(Ataque a) {
        UiFormatter.printSubtitle("EDITAR ATAQUE: " + a.getNome().toUpperCase());
        String nomeAntigo = a.getNome();

        String novoNome = InputUtil.readString("Novo Nome (em branco para manter '" + a.getNome() + "'): ");
        if (novoNome != null && !novoNome.trim().isEmpty()) {
            a.setNome(novoNome.trim());
        }

        String novaDesc = InputUtil.readString("Nova Descrição (em branco para manter '" + a.getDescricao() + "'): ");
        if (novaDesc != null && !novaDesc.trim().isEmpty()) {
            a.setDescricao(novaDesc.trim());
        }

        String novoDano = InputUtil.readString("Novo Dano (em branco para manter '" + a.getDano() + "'): ");
        if (novoDano != null && !novoDano.trim().isEmpty()) {
            a.setDano(novoDano.trim());
        }

        int novoNivel = InputUtil.readInt("Novo Nível (ou -1 para manter " + a.getNivel() + "): ", -1, 10);
        if (novoNivel >= 1) {
            a.setNivel(novoNivel);
        }

        System.out.println("Deseja alterar o Tipo/Magia? (s/n)");
        if (InputUtil.readBoolean("Opção: ")) {
            System.out.println("Selecione o Tipo:");
            System.out.println("1. Cronomacia");
            System.out.println("2. Abstractomacia");
            System.out.println("3. Necromacia");
            System.out.println("4. Espectromacia");
            System.out.println("5. Armas de curta distância");
            System.out.println("6. Armas de longa distância");
            System.out.println("7. Outro");
            int c = InputUtil.readInt("Opção: ", 1, 7);
            if (c == 1) a.setTipoMagiaOuArma("Cronomacia");
            else if (c == 2) a.setTipoMagiaOuArma("Abstractomacia");
            else if (c == 3) a.setTipoMagiaOuArma("Necromacia");
            else if (c == 4) a.setTipoMagiaOuArma("Espectromacia");
            else if (c == 5) a.setTipoMagiaOuArma("Armas de curta distância");
            else if (c == 6) a.setTipoMagiaOuArma("Armas de longa distância");
            else {
                String cust = InputUtil.readStringNotEmpty("Tipo: ");
                a.setTipoMagiaOuArma(cust);
            }
        }

        AtaqueFactory.atualizarNoCatalogo(nomeAntigo, a);
        UiFormatter.printSuccess("Ataque atualizado com sucesso! 💾");
    }

    // ==========================================
    // MENU DE COMÉRCIO E LOJAS
    // ==========================================
    private void menuComercioLojas() {
        while (true) {
            MenuBuilder menu = new MenuBuilder("🏪 SISTEMA DE COMÉRCIO & LOJAS 🏪");
            menu.addItem(1, "Ver Lojas por Cidade", "Listar lojas e ver estoque/descontos")
                .addItem(2, "Comprar Item de uma Loja", "Comprar item como um personagem")
                .addItem(3, "Vender Item para uma Loja", "Vender item da mochila")
                .addItem(4, "Buscar Item no Comércio", "Procurar onde vende mais barato (com frete)")
                .addItem(5, "Comércio entre Lojas (Mestre/Logística)", "Fazer loja comprar ou transferir itens")
                .addItem(6, "Atualizar Lojas (Simular Aventureiros)", "Executa simulação de mercado")
                .addItem(7, "Gerenciar Cidades, Redes e Lojas (Mestre)", "Criar cidades, redes e lojas")
                .addItem(0, "Voltar", "Menu principal");

            int opcao = menu.getUserChoice(0, 7);
            switch (opcao) {
                case 1:
                    verLojasPorCidade();
                    break;
                case 2:
                    comprarItemDeLoja();
                    break;
                case 3:
                    venderItemParaLoja();
                    break;
                case 4:
                    buscarItemNoComercio();
                    break;
                case 5:
                    menuComercioEntreLojas();
                    break;
                case 6:
                    simularMercadoLojas();
                    break;
                case 7:
                    menuGerenciamentoEntidades();
                    break;
                case 0:
                    return;
            }
        }
    }

    private void verLojasPorCidade() {
        List<Cidade> cidades = CidadeManager.listarTodas();
        if (cidades.isEmpty()) {
            UiFormatter.printWarning("Nenhuma cidade cadastrada!");
            return;
        }

        UiFormatter.printSubtitle("CIDADES DISPONÍVEIS");
        for (int i = 0; i < cidades.size(); i++) {
            Cidade c = cidades.get(i);
            System.out.printf("%d. %s (Região: %s, Riqueza: %.1f)\n", i + 1, c.getNome(), c.getRegiao(), c.getIndiceRiqueza());
        }
        System.out.println("0. Voltar");

        int selC = InputUtil.readInt("Selecione a cidade: ", 0, cidades.size());
        if (selC == 0) return;

        Cidade cidade = cidades.get(selC - 1);
        List<Loja> todasLojas = LojaManager.listarTodasLojas();
        List<Loja> lojasNaCidade = new ArrayList<>();
        for (Loja l : todasLojas) {
            if (l.getCidade().equalsIgnoreCase(cidade.getNome())) {
                lojasNaCidade.add(l);
            }
        }

        if (lojasNaCidade.isEmpty()) {
            UiFormatter.printWarning("Nenhuma loja cadastrada nesta cidade!");
            return;
        }

        UiFormatter.printSubtitle("LOJAS EM " + cidade.getNome().toUpperCase());
        for (int i = 0; i < lojasNaCidade.size(); i++) {
            Loja l = lojasNaCidade.get(i);
            String tipo = (l instanceof SedeRede) ? "Sede de Rede" : "Loja Individual";
            System.out.printf("%d. %s [%s] (Caixa: G$ %.2f, Poder: %d)\n", i + 1, l.getNome(), tipo, l.getCaixa(), l.getPoder());
        }
        System.out.println("0. Voltar");

        int selL = InputUtil.readInt("Selecione a loja para ver o catálogo: ", 0, lojasNaCidade.size());
        if (selL == 0) return;

        Loja loja = lojasNaCidade.get(selL - 1);
        
        FlashSaleManager.rolarDescontos(loja);

        UiFormatter.printSubtitle("CATÁLOGO: " + loja.getNome().toUpperCase());
        System.out.printf("Cidade: %s | Tipo: %s\n", loja.getCidade(), (loja instanceof SedeRede) ? "Sede" : "Individual");
        System.out.printf("Caixa: G$ %.2f | Poder: %d\n", loja.getCaixa(), loja.getPoder());
        System.out.println("----------------------------------------------");

        if (loja.getEstoque().isEmpty()) {
            System.out.println("  (Estoque vazio)");
        } else {
            for (Map.Entry<String, Integer> entry : loja.getEstoque().entrySet()) {
                Item item = ItemFactory.obterItemDoCatalogo(entry.getKey());
                if (item != null) {
                    double base = TradeNetworkManager.obterPrecoVendaLoja(loja, item);
                    double desc = FlashSaleManager.obterDescontoRelampago(loja.getId(), item.getNome());
                    double precoFinal = base * (1.0 - desc);
                    
                    String promoStr = desc > 0 ? String.format("%s [PROMOÇÃO! %.0f%% OFF! De G$ %.2f]%s", UiFormatter.GREEN, desc * 100.0, base, UiFormatter.RESET) : "";
                    System.out.printf("  • %s (Qtd: %d) | Poder: %d | Preço: G$ %.2f %s\n", 
                        UiFormatter.BOLD + item.getNome() + UiFormatter.RESET, 
                        entry.getValue(), 
                        item.getPoder(), 
                        precoFinal, 
                        promoStr);
                }
            }
        }
        System.out.println("----------------------------------------------");
        InputUtil.pressEnterToContinue();
    }

    private void comprarItemDeLoja() {
        Personagem p = selecionarPersonagem();
        if (p == null) return;

        List<Cidade> cidades = CidadeManager.listarTodas();
        if (cidades.isEmpty()) {
            UiFormatter.printWarning("Nenhuma cidade cadastrada!");
            return;
        }

        UiFormatter.printSubtitle("COMPRAR: SELECIONE A CIDADE DA LOJA");
        System.out.println("Cidade atual do personagem: " + p.getCidade());
        for (int i = 0; i < cidades.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, cidades.get(i).getNome());
        }
        System.out.println("0. Voltar");
        int selC = InputUtil.readInt("Cidade: ", 0, cidades.size());
        if (selC == 0) return;

        Cidade cidade = cidades.get(selC - 1);
        List<Loja> todasLojas = LojaManager.listarTodasLojas();
        List<Loja> lojasNaCidade = new ArrayList<>();
        for (Loja l : todasLojas) {
            if (l.getCidade().equalsIgnoreCase(cidade.getNome())) {
                lojasNaCidade.add(l);
            }
        }

        if (lojasNaCidade.isEmpty()) {
            UiFormatter.printWarning("Nenhuma loja cadastrada nesta cidade!");
            return;
        }

        UiFormatter.printSubtitle("COMPRAR: SELECIONE A LOJA");
        for (int i = 0; i < lojasNaCidade.size(); i++) {
            System.out.printf("%d. %s (Poder: %d)\n", i + 1, lojasNaCidade.get(i).getNome(), lojasNaCidade.get(i).getPoder());
        }
        System.out.println("0. Voltar");
        int selL = InputUtil.readInt("Loja: ", 0, lojasNaCidade.size());
        if (selL == 0) return;

        Loja loja = lojasNaCidade.get(selL - 1);

        if (loja.getEstoque().isEmpty()) {
            UiFormatter.printWarning("A loja não possui itens em estoque!");
            return;
        }

        FlashSaleManager.rolarDescontos(loja);

        List<String> itensEstoque = new ArrayList<>(loja.getEstoque().keySet());
        UiFormatter.printSubtitle("ITENS DISPONÍVEIS NA LOJA: " + loja.getNome().toUpperCase());
        System.out.printf("Saldo Jogador: G$ %.2f | Cidade Atual: %s\n", p.getInventario().getDinheiroG(), p.getCidade());
        if (!loja.getCidade().equalsIgnoreCase(p.getCidade())) {
            System.out.println(UiFormatter.YELLOW + "⚠️ Loja em outra cidade! Frete intermunicipal de G$ 15.00 será adicionado." + UiFormatter.RESET);
        }

        for (int i = 0; i < itensEstoque.size(); i++) {
            String itemChave = itensEstoque.get(i);
            Item item = ItemFactory.obterItemDoCatalogo(itemChave);
            int qtd = loja.getEstoque().get(itemChave);
            if (item != null) {
                double precoVenda = LojaEconomyManager.calcularPrecoVendaAoJogador(loja, item, p);
                double desc = FlashSaleManager.obterDescontoRelampago(loja.getId(), item.getNome());
                String promo = desc > 0 ? String.format(" (Promoção! %.0f%% OFF)", desc*100) : "";
                System.out.printf("%d. %s (Estoque: %d) - Preço Final: G$ %.2f%s\n", i + 1, item.getNome(), qtd, precoVenda, promo);
            }
        }
        System.out.println("0. Voltar");
        int selI = InputUtil.readInt("Selecione o item para comprar: ", 0, itensEstoque.size());
        if (selI == 0) return;

        String itemNome = itensEstoque.get(selI - 1);
        Item item = ItemFactory.obterItemDoCatalogo(itemNome);
        if (item != null) {
            double custo = LojaEconomyManager.calcularPrecoVendaAoJogador(loja, item, p);
            if (LojaEconomyManager.comprarDaLoja(p, loja, itemNome)) {
                UiFormatter.printSuccess("Compra realizada! Item '" + item.getNome() + "' adicionado à mochila por G$ " + custo);
            }
        }
        InputUtil.pressEnterToContinue();
    }

    private void venderItemParaLoja() {
        Personagem p = selecionarPersonagem();
        if (p == null) return;

        List<Item> mochilaItens = p.getInventario().getItens();
        if (mochilaItens.isEmpty()) {
            UiFormatter.printWarning("Sua mochila está vazia!");
            InputUtil.pressEnterToContinue();
            return;
        }

        List<Cidade> cidades = CidadeManager.listarTodas();
        if (cidades.isEmpty()) {
            UiFormatter.printWarning("Nenhuma cidade cadastrada!");
            return;
        }

        UiFormatter.printSubtitle("VENDER: SELECIONE A CIDADE DA LOJA");
        for (int i = 0; i < cidades.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, cidades.get(i).getNome());
        }
        System.out.println("0. Voltar");
        int selC = InputUtil.readInt("Cidade: ", 0, cidades.size());
        if (selC == 0) return;

        Cidade cidade = cidades.get(selC - 1);
        List<Loja> todasLojas = LojaManager.listarTodasLojas();
        List<Loja> lojasNaCidade = new ArrayList<>();
        for (Loja l : todasLojas) {
            if (l.getCidade().equalsIgnoreCase(cidade.getNome())) {
                lojasNaCidade.add(l);
            }
        }

        if (lojasNaCidade.isEmpty()) {
            UiFormatter.printWarning("Nenhuma loja cadastrada nesta cidade!");
            return;
        }

        UiFormatter.printSubtitle("VENDER: SELECIONE A LOJA");
        for (int i = 0; i < lojasNaCidade.size(); i++) {
            System.out.printf("%d. %s (Caixa: G$ %.2f, Poder: %d)\n", i + 1, lojasNaCidade.get(i).getNome(), lojasNaCidade.get(i).getCaixa(), lojasNaCidade.get(i).getPoder());
        }
        System.out.println("0. Voltar");
        int selL = InputUtil.readInt("Loja: ", 0, lojasNaCidade.size());
        if (selL == 0) return;

        Loja loja = lojasNaCidade.get(selL - 1);

        UiFormatter.printSubtitle("SEUS ITENS NA MOCHILA");
        for (int i = 0; i < mochilaItens.size(); i++) {
            Item item = mochilaItens.get(i);
            double precoCompraLoja = LojaEconomyManager.calcularPrecoCompraDoJogador(loja, item, p);
            System.out.printf("%d. %s (Poder: %d) - Loja paga: G$ %.2f\n", i + 1, item.getNome(), item.getPoder(), precoCompraLoja);
        }
        System.out.println("0. Voltar");
        int selI = InputUtil.readInt("Selecione o item para vender: ", 0, mochilaItens.size());
        if (selI == 0) return;

        Item item = mochilaItens.get(selI - 1);
        double preco = LojaEconomyManager.calcularPrecoCompraDoJogador(loja, item, p);
        if (LojaEconomyManager.venderParaLoja(p, loja, item)) {
            UiFormatter.printSuccess("Venda concluída! Você vendeu '" + item.getNome() + "' por G$ " + preco);
        }
        InputUtil.pressEnterToContinue();
    }

    private void buscarItemNoComercio() {
        String nomeItem = InputUtil.readStringNotEmpty("Digite o nome ou parte do nome do item: ").trim().toLowerCase();
        
        List<Loja> todasLojas = LojaManager.listarTodasLojas();
        List<Loja> vendedores = new ArrayList<>();
        for (Loja l : todasLojas) {
            for (String key : l.getEstoque().keySet()) {
                if (key.contains(nomeItem)) {
                    vendedores.add(l);
                    break;
                }
            }
        }

        if (vendedores.isEmpty()) {
            UiFormatter.printWarning("Nenhum estabelecimento comercial possui este item no estoque!");
            InputUtil.pressEnterToContinue();
            return;
        }

        Personagem p = selecionarPersonagem();
        if (p == null) return;

        class LojaPreco {
            Loja loja;
            Item item;
            double precoFinal;
            int estoque;
        }

        List<LojaPreco> list = new ArrayList<>();
        for (Loja l : vendedores) {
            for (String key : l.getEstoque().keySet()) {
                if (key.contains(nomeItem)) {
                    Item item = ItemFactory.obterItemDoCatalogo(key);
                    if (item != null) {
                        LojaPreco lp = new LojaPreco();
                        lp.loja = l;
                        lp.item = item;
                        lp.precoFinal = LojaEconomyManager.calcularPrecoVendaAoJogador(l, item, p);
                        lp.estoque = l.getEstoque().get(key);
                        list.add(lp);
                    }
                }
            }
        }

        list.sort(Comparator.comparingDouble(lp -> lp.precoFinal));

        UiFormatter.printSubtitle("RESULTADO DA BUSCA DE ITENS (MAIS BARATOS PRIMEIRO)");
        System.out.println("Sua cidade atual: " + p.getCidade());
        for (int i = 0; i < list.size(); i++) {
            LojaPreco lp = list.get(i);
            String freteStr = lp.loja.getCidade().equalsIgnoreCase(p.getCidade()) ? "" : " (G$ +15.00 frete incluso)";
            System.out.printf("%d. %s em '%s' (%s) - Qtd: %d | Preço Final: G$ %.2f%s\n", 
                i + 1, 
                lp.item.getNome(), 
                lp.loja.getNome(), 
                lp.loja.getCidade(), 
                lp.estoque, 
                lp.precoFinal, 
                freteStr);
        }

        System.out.println("\nDeseja comprar algum item diretamente?");
        System.out.println("Selecione o número correspondente (ou 0 para Cancelar):");
        int opt = InputUtil.readInt("Opção: ", 0, list.size());
        if (opt == 0) return;

        LojaPreco selecionado = list.get(opt - 1);
        if (LojaEconomyManager.comprarDaLoja(p, selecionado.loja, selecionado.item.getNome())) {
            UiFormatter.printSuccess("Item '" + selecionado.item.getNome() + "' comprado com sucesso!");
        }
        InputUtil.pressEnterToContinue();
    }

    private void menuComercioEntreLojas() {
        while (true) {
            UiFormatter.printSubtitle("LOGÍSTICA E COMÉRCIO ENTRE ESTABELECIMENTOS");
            System.out.println("1. Transferência ao Custo (Mesma Rede de Lojas)");
            System.out.println("2. Reabastecimento por Compra (Procurement / Loja Individual Compra de Fornecedor)");
            System.out.println("0. Voltar");

            int opt = InputUtil.readInt("Opção: ", 0, 2);
            if (opt == 0) return;

            List<Loja> lojas = LojaManager.listarTodasLojas();
            if (lojas.isEmpty()) {
                System.out.println("Nenhuma loja cadastrada!");
                continue;
            }

            if (opt == 1) {
                List<SedeRede> sedes = new ArrayList<>();
                for (Loja l : lojas) {
                    if (l instanceof SedeRede) {
                        sedes.add((SedeRede) l);
                    }
                }

                if (sedes.size() < 2) {
                    UiFormatter.printWarning("Necessário ter pelo menos 2 Sedes de Rede cadastradas!");
                    continue;
                }

                System.out.println("Escolha a SEDE DE ORIGEM (Quem envia o item):");
                for (int i = 0; i < sedes.size(); i++) {
                    System.out.printf("%d. %s (Rede: %s, Caixa: G$ %.2f)\n", i + 1, sedes.get(i).getNome(), sedes.get(i).getRedeId(), sedes.get(i).getCaixa());
                }
                int selO = InputUtil.readInt("Origem: ", 1, sedes.size());
                SedeRede origem = sedes.get(selO - 1);

                if (origem.getEstoque().isEmpty()) {
                    System.out.println("Estoque da origem está vazio!");
                    continue;
                }

                System.out.println("Escolha a SEDE DE DESTINO (Quem recebe o item):");
                List<SedeRede> destinosElegiveis = new ArrayList<>();
                for (SedeRede s : sedes) {
                    if (s.getRedeId().equalsIgnoreCase(origem.getRedeId()) && !s.getId().equals(origem.getId())) {
                        destinosElegiveis.add(s);
                    }
                }

                if (destinosElegiveis.isEmpty()) {
                    UiFormatter.printWarning("Nenhuma outra Sede cadastrada na mesma rede '" + origem.getRedeId() + "'!");
                    continue;
                }

                for (int i = 0; i < destinosElegiveis.size(); i++) {
                    System.out.printf("%d. %s (Caixa: G$ %.2f)\n", i + 1, destinosElegiveis.get(i).getNome(), destinosElegiveis.get(i).getCaixa());
                }
                int selD = InputUtil.readInt("Destino: ", 1, destinosElegiveis.size());
                SedeRede destino = destinosElegiveis.get(selD - 1);

                List<String> estoqueList = new ArrayList<>(origem.getEstoque().keySet());
                System.out.println("Selecione o item para transferir:");
                for (int i = 0; i < estoqueList.size(); i++) {
                    String chave = estoqueList.get(i);
                    System.out.printf("%d. %s (Qtd: %d)\n", i + 1, chave, origem.getEstoque().get(chave));
                }
                int selI = InputUtil.readInt("Item: ", 1, estoqueList.size());
                String itemNome = estoqueList.get(selI - 1);
                
                int maxQ = origem.getEstoque().get(itemNome);
                int qtd = InputUtil.readInt("Quantidade para transferir (máx " + maxQ + "): ", 1, maxQ);

                if (TradeNetworkManager.transferirEntreSedes(origem, destino, itemNome, qtd)) {
                    UiFormatter.printSuccess("Transferência logística de " + qtd + "x '" + itemNome + "' concluída com sucesso!");
                } else {
                    UiFormatter.printError("Falha ao realizar transferência. Verifique fundos do destino.");
                }

            } else if (opt == 2) {
                List<LojaIndividual> individuais = new ArrayList<>();
                for (Loja l : lojas) {
                    if (l instanceof LojaIndividual) {
                        individuais.add((LojaIndividual) l);
                    }
                }

                if (individuais.isEmpty()) {
                    UiFormatter.printWarning("Nenhuma loja individual cadastrada!");
                    continue;
                }

                System.out.println("Escolha a LOJA COMPRADORA (Loja Individual):");
                for (int i = 0; i < individuais.size(); i++) {
                    System.out.printf("%d. %s (Caixa: G$ %.2f)\n", i + 1, individuais.get(i).getNome(), individuais.get(i).getCaixa());
                }
                int selC = InputUtil.readInt("Comprador: ", 1, individuais.size());
                LojaIndividual comprador = individuais.get(selC - 1);

                System.out.println("Tipo de busca por fornecedor:");
                System.out.println("1. Manual (Escolher fornecedor e item)");
                System.out.println("2. Automático (Procurement - Comprar do mais barato do comércio)");
                int modo = InputUtil.readInt("Opção: ", 1, 2);

                if (modo == 1) {
                    System.out.println("Escolha o FORNECEDOR (Qualquer outra loja):");
                    List<Loja> fornecedores = new ArrayList<>(lojas);
                    fornecedores.remove(comprador);

                    for (int i = 0; i < fornecedores.size(); i++) {
                        System.out.printf("%d. %s (Cidade: %s)\n", i + 1, fornecedores.get(i).getNome(), fornecedores.get(i).getCidade());
                    }
                    int selF = InputUtil.readInt("Fornecedor: ", 1, fornecedores.size());
                    Loja fornecedor = fornecedores.get(selF - 1);

                    if (fornecedor.getEstoque().isEmpty()) {
                        System.out.println("Fornecedor está com estoque vazio!");
                        continue;
                    }

                    List<String> estoqueF = new ArrayList<>(fornecedor.getEstoque().keySet());
                    System.out.println("Selecione o item para comprar:");
                    for (int i = 0; i < estoqueF.size(); i++) {
                        String chave = estoqueF.get(i);
                        Item item = ItemFactory.obterItemDoCatalogo(chave);
                        double precoVenda = TradeNetworkManager.obterPrecoVendaLoja(fornecedor, item);
                        System.out.printf("%d. %s (Qtd: %d) - Preço de Venda: G$ %.2f\n", i + 1, chave, fornecedor.getEstoque().get(chave), precoVenda);
                    }
                    int selI = InputUtil.readInt("Item: ", 1, estoqueF.size());
                    String itemNome = estoqueF.get(selI - 1);
                    int maxQ = fornecedor.getEstoque().get(itemNome);
                    int qtd = InputUtil.readInt("Quantidade (máx " + maxQ + "): ", 1, maxQ);

                    if (TradeNetworkManager.comprarDeFornecedor(comprador, fornecedor, itemNome, qtd)) {
                        UiFormatter.printSuccess("Compra efetuada! Loja Individual reabastecida.");
                    } else {
                        UiFormatter.printError("Falha na compra. Verifique caixa da loja compradora.");
                    }
                } else {
                    String itemNome = InputUtil.readStringNotEmpty("Nome do item para procurement automático: ").trim().toLowerCase();
                    Optional<Loja> melhorFornecedor = TradeNetworkManager.encontrarMelhorFornecedor(itemNome, lojas);
                    if (melhorFornecedor.isEmpty()) {
                        System.out.println("Nenhuma loja comercializa o item '" + itemNome + "' no momento!");
                        continue;
                    }
                    Loja fornecedor = melhorFornecedor.get();
                    if (fornecedor.getId().equals(comprador.getId())) {
                        System.out.println("O melhor fornecedor é a própria loja compradora!");
                        continue;
                    }

                    Item item = ItemFactory.obterItemDoCatalogo(itemNome);
                    double precoUnit = TradeNetworkManager.obterPrecoVendaLoja(fornecedor, item);
                    int maxQ = fornecedor.getEstoque().get(itemNome);
                    System.out.printf("Melhor Fornecedor Encontrado: %s em %s (Preço unitário: G$ %.2f, Disponível: %d)\n", fornecedor.getNome(), fornecedor.getCidade(), precoUnit, maxQ);
                    
                    int qtd = InputUtil.readInt("Quantidade para reabastecer: ", 1, maxQ);
                    if (TradeNetworkManager.comprarDeFornecedor(comprador, fornecedor, itemNome, qtd)) {
                        UiFormatter.printSuccess("Logística Automática Concluída! Compra realizada de " + fornecedor.getNome());
                    } else {
                        UiFormatter.printError("Falha ao comprar. Fundos insuficientes.");
                    }
                }
            }
        }
    }

    private void simularMercadoLojas() {
        UiFormatter.printSubtitle("EXECUTANDO SIMULAÇÃO DE ATUALIZAÇÃO DO MERCADO");
        System.out.println("Escolha o escopo de atualização:");
        System.out.println("1. Atualizar TODAS as lojas do mundo");
        System.out.println("2. Atualizar uma Loja específica");
        int esc = InputUtil.readInt("Opção: ", 1, 2);

        List<String> report = new ArrayList<>();
        if (esc == 1) {
            report = MarketSimulationManager.atualizarLojas();
        } else {
            List<Loja> lojas = LojaManager.listarTodasLojas();
            if (lojas.isEmpty()) {
                System.out.println("Nenhuma loja cadastrada!");
                return;
            }
            for (int i = 0; i < lojas.size(); i++) {
                System.out.printf("%d. %s (%s)\n", i + 1, lojas.get(i).getNome(), lojas.get(i).getCidade());
            }
            int idx = InputUtil.readInt("Opção: ", 1, lojas.size());
            Loja l = lojas.get(idx - 1);
            report = MarketSimulationManager.atualizarLoja(l);
            LojaManager.salvarLojas();
        }

        UiFormatter.printSubtitle("RELATÓRIO DE EVENTOS DE SIMULAÇÃO DE MERCADO");
        if (report.isEmpty()) {
            System.out.println("Nenhum evento registrado nesta simulação.");
        } else {
            for (String log : report) {
                System.out.println("  • " + log);
            }
        }
        InputUtil.pressEnterToContinue();
    }

    private void menuGerenciamentoEntidades() {
        while (true) {
            UiFormatter.printSubtitle("GERENCIAR CIDADES, REDES E LOJAS (MODO MESTRE)");
            System.out.println("1. Gerenciar Cidades (Criar, Editar, Excluir, Listar)");
            System.out.println("2. Criar Nova Rede de Lojas");
            System.out.println("3. Listar Redes");
            System.out.println("4. Criar Nova Loja (Sede ou Individual)");
            System.out.println("5. Listar Lojas Globais");
            System.out.println("6. Adicionar Item ao Estoque de uma Loja");
            System.out.println("7. Excluir Loja do Catálogo");
            System.out.println("0. Voltar");

            int opt = InputUtil.readInt("Opção: ", 0, 7);
            if (opt == 0) return;

            if (opt == 1) {
                menuGerenciarCidades();

            } else if (opt == 2) {
                String id = InputUtil.readStringNotEmpty("ID da Rede (slug): ").toLowerCase();
                String nome = InputUtil.readStringNotEmpty("Nome da Rede: ");
                LojaManager.adicionarRede(new RedeLoja(id, nome, new ArrayList<>()));
                UiFormatter.printSuccess("Rede de Lojas '" + nome + "' criada!");

            } else if (opt == 3) {
                List<RedeLoja> list = LojaManager.listarTodasRedes();
                UiFormatter.printSubtitle("REDES REGISTRADAS");
                for (RedeLoja r : list) {
                    System.out.printf("- %s [ID: %s] (%d sedes)\n", r.getNomeRede(), r.getId(), r.getSedeIds().size());
                }
                InputUtil.pressEnterToContinue();

            } else if (opt == 4) {
                List<Cidade> cidades = CidadeManager.listarTodas();
                if (cidades.isEmpty()) {
                    UiFormatter.printError("Crie uma cidade primeiro!");
                    continue;
                }

                String id = InputUtil.readStringNotEmpty("ID da Loja (slug): ").toLowerCase();
                String nome = InputUtil.readStringNotEmpty("Nome da Loja: ");
                
                System.out.println("Selecione a Cidade:");
                for (int i = 0; i < cidades.size(); i++) {
                    System.out.printf("%d. %s\n", i + 1, cidades.get(i).getNome());
                }
                int selC = InputUtil.readInt("Cidade: ", 1, cidades.size());
                String cidadeNome = cidades.get(selC - 1).getNome();

                double caixa = InputUtil.readDouble("Capital Inicial (Caixa G$): ");

                System.out.println("Tipo de Estabelecimento:");
                System.out.println("1. Sede de Rede de Lojas");
                System.out.println("2. Loja Individual");
                int t = InputUtil.readInt("Opção: ", 1, 2);

                if (t == 1) {
                    List<RedeLoja> redes = LojaManager.listarTodasRedes();
                    if (redes.isEmpty()) {
                        UiFormatter.printError("Crie uma rede primeiro!");
                        continue;
                    }
                    System.out.println("Selecione a Rede pertencente:");
                    for (int i = 0; i < redes.size(); i++) {
                        System.out.printf("%d. %s\n", i + 1, redes.get(i).getNomeRede());
                    }
                    int selR = InputUtil.readInt("Rede: ", 1, redes.size());
                    RedeLoja rede = redes.get(selR - 1);

                    SedeRede sr = new SedeRede(id, nome, cidadeNome, caixa, rede.getId());
                    LojaManager.adicionarLoja(sr);
                    
                    rede.getSedeIds().add(id);
                    LojaManager.salvarRedes();
                    UiFormatter.printSuccess("Sede de Rede '" + nome + "' adicionada e vinculada à rede '" + rede.getNomeRede() + "'!");
                } else {
                    double margem = InputUtil.readDouble("Margem de Lucro padrão (ex: 0.35 para 35%): ");
                    String dono = InputUtil.readString("Nome do Dono (opcional): ");
                    LojaIndividual li = new LojaIndividual(id, nome, cidadeNome, caixa, margem, dono);
                    LojaManager.adicionarLoja(li);
                    UiFormatter.printSuccess("Loja Individual '" + nome + "' cadastrada com sucesso!");
                }

            } else if (opt == 5) {
                List<Loja> list = LojaManager.listarTodasLojas();
                UiFormatter.printSubtitle("LOJAS REGISTRADAS NO MUNDO");
                for (Loja l : list) {
                    String tipo = (l instanceof SedeRede) ? "Sede" : "Individual";
                    System.out.printf("- %s [ID: %s] | Cidade: %s | Tipo: %s | Caixa: G$ %.2f | Poder: %d\n", 
                        l.getNome(), l.getId(), l.getCidade(), tipo, l.getCaixa(), l.getPoder());
                }
                InputUtil.pressEnterToContinue();

            } else if (opt == 6) {
                List<Loja> lojas = LojaManager.listarTodasLojas();
                if (lojas.isEmpty()) {
                    System.out.println("Nenhuma loja cadastrada!");
                    continue;
                }
                System.out.println("Selecione a Loja:");
                for (int i = 0; i < lojas.size(); i++) {
                    System.out.printf("%d. %s (%s)\n", i + 1, lojas.get(i).getNome(), lojas.get(i).getCidade());
                }
                int selL = InputUtil.readInt("Loja: ", 1, lojas.size());
                Loja loja = lojas.get(selL - 1);

                List<Item> catalogo = ItemFactory.listarCatalogo();
                if (catalogo.isEmpty()) {
                    System.out.println("Catálogo global de itens vazio!");
                    continue;
                }

                System.out.println("Selecione o Item para adicionar ao estoque da loja:");
                for (int i = 0; i < catalogo.size(); i++) {
                    System.out.printf("%d. %s (Poder: %d)\n", i + 1, catalogo.get(i).getNome(), catalogo.get(i).getPoder());
                }
                int selI = InputUtil.readInt("Item: ", 1, catalogo.size());
                Item item = catalogo.get(selI - 1);

                int qtd = InputUtil.readInt("Quantidade para adicionar: ", 1, 100);
                loja.getEstoque().put(item.getNome().toLowerCase(), loja.getEstoque().getOrDefault(item.getNome().toLowerCase(), 0) + qtd);
                
                loja.setPoder(ShopPowerCalculator.calcularPoder(loja));
                LojaManager.salvarLojas();
                UiFormatter.printSuccess("Adicionado " + qtd + "x '" + item.getNome() + "' ao estoque de '" + loja.getNome() + "'. Poder atualizado para " + loja.getPoder() + "!");

            } else if (opt == 7) {
                List<Loja> lojas = LojaManager.listarTodasLojas();
                if (lojas.isEmpty()) {
                    System.out.println("Nenhuma loja cadastrada!");
                    continue;
                }
                System.out.println("Selecione a Loja para Excluir:");
                for (int i = 0; i < lojas.size(); i++) {
                    System.out.printf("%d. %s [ID: %s]\n", i + 1, lojas.get(i).getNome(), lojas.get(i).getId());
                }
                int selL = InputUtil.readInt("Excluir: ", 1, lojas.size());
                Loja l = lojas.get(selL - 1);
                
                if (InputUtil.readBoolean("Deseja realmente remover '" + l.getNome() + "' permanentemente? (s/n): ")) {
                    LojaManager.removerLoja(l.getId());
                    UiFormatter.printSuccess("Loja removida com sucesso!");
                }
            }
        }
    }

    private void menuGerenciarCidades() {
        while (true) {
            UiFormatter.printSubtitle("GERENCIAR CIDADES 🗺️");
            System.out.println("1. Criar Nova Cidade");
            System.out.println("2. Listar Cidades");
            System.out.println("3. Editar Cidade");
            System.out.println("4. Excluir Cidade");
            System.out.println("0. Voltar");

            int opt = InputUtil.readInt("Opção: ", 0, 4);
            if (opt == 0) return;

            if (opt == 1) {
                String nome = InputUtil.readStringNotEmpty("Nome da Cidade: ");
                if (CidadeManager.obter(nome) != null) {
                    UiFormatter.printError("Uma cidade com este nome já existe!");
                    continue;
                }
                String regiao = InputUtil.readStringNotEmpty("Região: ");
                double riqueza = InputUtil.readDouble("Índice de Riqueza (0.5 a 2.0): ");
                CidadeManager.adicionar(new Cidade(nome, regiao, riqueza));
                UiFormatter.printSuccess("Cidade '" + nome + "' adicionada!");
            } else if (opt == 2) {
                List<Cidade> list = CidadeManager.listarTodas();
                UiFormatter.printSubtitle("CIDADES REGISTRADAS");
                if (list.isEmpty()) {
                    System.out.println("(Nenhuma cidade cadastrada)");
                } else {
                    for (Cidade c : list) {
                        System.out.printf("- %s (Região: %s, Riqueza: %.1f)\n", c.getNome(), c.getRegiao(), c.getIndiceRiqueza());
                    }
                }
                InputUtil.pressEnterToContinue();
            } else if (opt == 3) {
                List<Cidade> list = CidadeManager.listarTodas();
                if (list.isEmpty()) {
                    System.out.println("Nenhuma cidade cadastrada!");
                    continue;
                }
                System.out.println("Selecione a cidade para editar:");
                for (int i = 0; i < list.size(); i++) {
                    System.out.printf("%d. %s\n", i + 1, list.get(i).getNome());
                }
                int idx = InputUtil.readInt("Opção: ", 1, list.size());
                Cidade c = list.get(idx - 1);
                String antigoNome = c.getNome();

                String novoNome = InputUtil.readString("Novo Nome (em branco para manter '" + c.getNome() + "'): ");
                if (novoNome != null && !novoNome.trim().isEmpty()) {
                    c.setNome(novoNome.trim());
                }

                String novaRegiao = InputUtil.readString("Nova Região (em branco para manter '" + c.getRegiao() + "'): ");
                if (novaRegiao != null && !novaRegiao.trim().isEmpty()) {
                    c.setRegiao(novaRegiao.trim());
                }

                Double novaRiqueza = InputUtil.readOptionalDouble("Novo Índice de Riqueza (em branco para manter " + c.getIndiceRiqueza() + "): ");
                if (novaRiqueza != null) {
                    c.setIndiceRiqueza(novaRiqueza);
                }

                CidadeManager.remover(antigoNome);
                CidadeManager.adicionar(c);
                UiFormatter.printSuccess("Cidade atualizada com sucesso!");
            } else if (opt == 4) {
                List<Cidade> list = CidadeManager.listarTodas();
                if (list.isEmpty()) {
                    System.out.println("Nenhuma cidade cadastrada!");
                    continue;
                }
                System.out.println("Selecione a cidade para excluir:");
                for (int i = 0; i < list.size(); i++) {
                    System.out.printf("%d. %s\n", i + 1, list.get(i).getNome());
                }
                int idx = InputUtil.readInt("Opção: ", 1, list.size());
                Cidade c = list.get(idx - 1);
                if (InputUtil.readBoolean("Deseja realmente remover a cidade '" + c.getNome() + "' permanentemente? (s/n): ")) {
                    CidadeManager.remover(c.getNome());
                    UiFormatter.printSuccess("Cidade removida com sucesso!");
                }
            }
        }
    }

    private void gerenciarPassivasPersonagem(Personagem p) {
        while (true) {
            UiFormatter.printSubtitle("PASSIVAS DE " + p.getNome().toUpperCase());
            List<Passiva> passivas = p.getPassivas();
            if (passivas.isEmpty()) {
                System.out.println("(Nenhuma passiva atribuída)");
            } else {
                for (int i = 0; i < passivas.size(); i++) {
                    Passiva pass = passivas.get(i);
                    System.out.printf("%d. %s [%s] - Condição: %s | Efeito: %s\n", 
                        i + 1, pass.getNome(), pass.getTipoMagiaOuArma(), pass.getCondicao(), pass.getEfeito());
                }
            }
            
            System.out.println("\nOpções:");
            System.out.println("1. Adicionar Passiva do Catálogo");
            System.out.println("2. Remover Passiva do Personagem");
            System.out.println("0. Voltar");
            
            int escolha = InputUtil.readInt("Opção: ", 0, 2);
            if (escolha == 0) return;
            
            if (escolha == 1) {
                List<Passiva> catalogo = PassivaFactory.listarCatalogo();
                if (catalogo.isEmpty()) {
                    System.out.println("Não existem passivas cadastradas no catálogo!");
                    InputUtil.pressEnterToContinue();
                    continue;
                }
                
                System.out.println("\n--- PASSIVAS DISPONÍVEIS NO CATÁLOGO ---");
                for (int i = 0; i < catalogo.size(); i++) {
                    System.out.printf("%d. %s [%s] - Condição: %s | Efeito: %s\n", 
                        i + 1, catalogo.get(i).getNome(), catalogo.get(i).getTipoMagiaOuArma(), 
                        catalogo.get(i).getCondicao(), catalogo.get(i).getEfeito());
                }
                int idx = InputUtil.readInt("Escolha a passiva para adicionar: ", 1, catalogo.size());
                Passiva selecionada = catalogo.get(idx - 1);
                
                boolean jaTem = false;
                for (Passiva pass : passivas) {
                    if (pass.getNome().equalsIgnoreCase(selecionada.getNome())) {
                        jaTem = true;
                        break;
                    }
                }
                
                if (jaTem) {
                    UiFormatter.printWarning("O personagem já possui esta passiva!");
                } else {
                    passivas.add(PassivaFactory.clonarPassiva(selecionada));
                    UiFormatter.printSuccess("Passiva '" + selecionada.getNome() + "' adicionada!");
                }
            } else if (escolha == 2) {
                if (passivas.isEmpty()) {
                    System.out.println("O personagem não tem passivas para remover.");
                    continue;
                }
                int idx = InputUtil.readInt("Escolha a passiva para remover: ", 1, passivas.size());
                Passiva removida = passivas.remove(idx - 1);
                UiFormatter.printSuccess("Passiva '" + removida.getNome() + "' removida!");
            }
        }
    }
}
