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
                .addItem(3, "Magias e Combate", "Arena de Duelo")
                .addItem(4, "Tabelas Globais", "Raças, Classes, Magias")
                .addItem(5, "Mercado & Lojinha", "Comprar e vender itens")
                .addItem(6, "Importação e Criação de Itens", "Importar CSV ou criar item catalogo")
                .addItem(0, "Sair", "Encerrar jogo");

            int opcao = menu.getUserChoice(0, 6);

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
                    menuMercado();
                    break;
                case 6:
                    menuImportacaoItens();
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
        System.out.println("0. Voltar");

        int opcao = InputUtil.readInt("Opção: ", 0, 6);

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
            case 0:
                return;
        }

        StatusManager.recalcularStatus(p);
        repository.salvar(p);
        System.out.println("Alterações aplicadas e salvas com sucesso!");
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
            MenuBuilder menu = new MenuBuilder("✨ MAGIAS, RESISTÊNCIAS E ARENA ✨");
            menu.addItem(1, "Ensinar Magia", "Aprender novo poder")
                .addItem(2, "Esquecer Magia", "Desaprender poder")
                .addItem(3, "Simular Duelo", "Combate na Arena")
                .addItem(0, "Voltar", "Menu anterior");

            int opcao = menu.getUserChoice(0, 3);

            switch (opcao) {
                case 1:
                    ensinarMagia();
                    break;
                case 2:
                    esquecerMagia();
                    break;
                case 3:
                    simularArena();
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

    private void simularArena() {
        List<Personagem> personagens = repository.listarTodos();
        carregarMonstros();

        if (personagens.isEmpty()) {
            UiFormatter.printWarning("Necessário possuir ao menos 1 personagem salvo para usar a Arena!");
            return;
        }

        UiFormatter.printSubtitle("ARENA DE DUELO ⚔️");
        System.out.println("Escolha o ATACANTE (Jogador):");
        for (int i = 0; i < personagens.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, personagens.get(i).getNome());
        }
        System.out.println("0. Voltar");
        int idxA = InputUtil.readInt("Atacante (ou 0 para Voltar): ", 0, personagens.size());
        if (idxA == 0) return;
        Personagem atacante = clonarPersonagem(personagens.get(idxA - 1));

        System.out.println("Escolha o DEFENSOR:");
        System.out.println("--- JOGADORES ---");
        int count = 1;
        for (int i = 0; i < personagens.size(); i++) {
            System.out.printf("%d. Jogador: %s\n", count++, personagens.get(i).getNome());
        }
        System.out.println("--- MONSTROS CATALOGADOS ---");
        for (int i = 0; i < monstros.size(); i++) {
            System.out.printf("%d. Monstro: %s (Tipo: %s)\n", count++, monstros.get(i).getNome(), monstros.get(i).getTipo());
        }
        System.out.println("0. Voltar");
        int idxD = InputUtil.readInt("Defensor (ou 0 para Voltar): ", 0, count - 1);
        if (idxD == 0) return;

        EntidadeRPG defensor;
        boolean defensorEhMonstro = false;
        Monstro monstroDefensor = null;

        if (idxD <= personagens.size()) {
            defensor = clonarPersonagem(personagens.get(idxD - 1));
        } else {
            monstroDefensor = monstros.get(idxD - personagens.size() - 1);
            try {
                ObjectMapper mapper = new ObjectMapper();
                String raw = mapper.writeValueAsString(monstroDefensor);
                defensor = mapper.readValue(raw, Monstro.class);
            } catch (Exception e) {
                defensor = monstroDefensor;
            }
            defensorEhMonstro = true;
        }

        EventBus.getInstance().publish(new br.com.frcli.event.CombateIniciadoEvent(atacante, defensor));

        UiFormatter.printTitle("⚔️ DUELO ATÉ A MORTE ⚔️");
        System.out.printf("%s (%s) VS %s\n", 
            UiFormatter.BOLD + UiFormatter.RED + atacante.getNome() + UiFormatter.RESET, 
            atacante.getStatusFinalAtributo("vida") + " HP",
            UiFormatter.BOLD + UiFormatter.BLUE + defensor.getNome() + UiFormatter.RESET + " (" + defensor.getStatusFinalAtributo("vida") + " HP)");

        Random r = new Random();
        int turno = 1;
        double hpA = atacante.getStatusFinalAtributo("vida");
        double hpD = defensor.getStatusFinalAtributo("vida");

        double maxHpA = hpA;
        double maxHpD = hpD;

        while (hpA > 0 && hpD > 0) {
            System.out.println("\n" + UiFormatter.BOLD + "=== ROUND " + turno + " ===" + UiFormatter.RESET);

            int d20A = r.nextInt(20) + 1;
            int d20D = r.nextInt(20) + 1;
            double initA = d20A + atacante.getStatusFinalAtributo("velocidade");
            double initD = d20D + defensor.getStatusFinalAtributo("velocidade");

            System.out.printf("🎲 Iniciativa %s: d20(%d) + Vel(%.1f) = %.1f\n", atacante.getNome(), d20A, atacante.getStatusFinalAtributo("velocidade"), initA);
            System.out.printf("🎲 Iniciativa %s: d20(%d) + Vel(%.1f) = %.1f\n", defensor.getNome(), d20D, defensor.getStatusFinalAtributo("velocidade"), initD);

            EntidadeRPG primeiro = (initA >= initD) ? atacante : defensor;
            EntidadeRPG segundo = (initA >= initD) ? defensor : atacante;

            // Primeiro ataca
            if (primeiro == atacante) {
                hpD = executarAcaoTurno(atacante, defensor, hpA, hpD, r);
            } else {
                hpA = executarAcaoTurno(defensor, atacante, hpD, hpA, r);
            }
            if (hpA <= 0 || hpD <= 0) break;

            // Segundo ataca
            if (segundo == atacante) {
                hpD = executarAcaoTurno(atacante, defensor, hpA, hpD, r);
            } else {
                hpA = executarAcaoTurno(defensor, atacante, hpD, hpA, r);
            }

            EventBus.getInstance().publish(new br.com.frcli.event.TurnoFinalizadoEvent(atacante, turno));
            if (!defensorEhMonstro) {
                EventBus.getInstance().publish(new br.com.frcli.event.TurnoFinalizadoEvent((Personagem) defensor, turno));
            }

            System.out.printf("\n[FIM DO ROUND %d] Saldo HP: %s: %.1f/%.1f | %s: %.1f/%.1f\n", 
                turno, atacante.getNome(), hpA, maxHpA, defensor.getNome(), hpD, maxHpD);

            turno++;
            InputUtil.pressEnterToContinue();
        }

        EntidadeRPG vencedor = hpA > 0 ? atacante : defensor;
        EntidadeRPG perdedor = hpA > 0 ? defensor : atacante;

        UiFormatter.printSuccess("COMBATE TERMINADO! Vencedor: " + vencedor.getNome() + "! 🎉");
        EventBus.getInstance().publish(new br.com.frcli.event.EntidadeDerrotadaEvent(perdedor));

        if (defensorEhMonstro && hpA > 0) {
            LootManager.LootResult loot = LootManager.gerarLoot((Monstro) defensor);
            UiFormatter.printSubtitle("💎 RECOMPENSAS DO COMBATE");
            System.out.printf("  Moedas G$ ganhas: G$ %.2f\n", loot.moedasG);
            if (!loot.itens.isEmpty()) {
                System.out.println("  Itens dropados:");
                for (Item item : loot.itens) {
                    System.out.println("    * " + item.getNome() + " (" + item.getDescricao() + ")");
                }
            } else {
                System.out.println("  Nenhum item dropado.");
            }

            if (InputUtil.readBoolean("Deseja coletar todo o loot para a mochila do atacante? (s/n): ")) {
                Personagem jogadorReal = personagens.get(idxA - 1);
                try {
                    InventoryManager.adicionarG(jogadorReal.getInventario(), loot.moedasG, false);
                    System.out.println("G$ depositados na carteira.");
                } catch (Exception e) {
                    System.out.println("Erro ao depositar moedas.");
                }

                for (Item item : loot.itens) {
                    try {
                        InventoryManager.adicionarItem(jogadorReal.getInventario(), item);
                        System.out.println("Item '" + item.getNome() + "' guardado na mochila.");
                    } catch (MochilaCheiaException e) {
                        UiFormatter.printError("Erro: Mochila cheia! Não foi possível pegar '" + item.getNome() + "'.");
                        break;
                    }
                }
                repository.salvar(jogadorReal);
            }
        }
        InputUtil.pressEnterToContinue();
    }

    private double executarAcaoTurno(EntidadeRPG atacante, EntidadeRPG defensor, double hpAtacante, double hpDefensor, Random r) {
        System.out.println("\n👉 Turno de " + atacante.getNome() + ":");

        if (atacante instanceof Monstro) {
            Monstro m = (Monstro) atacante;
            String ataque = m.getAtaquesFixos().get(r.nextInt(m.getAtaquesFixos().size()));
            System.out.println("👾 O monstro usa: " + ataque);

            double danoBase = m.getStatusFinalAtributo("forca") * 1.5 + 10;
            return processarAtaqueSimulado(atacante.getNome(), defensor, hpDefensor, danoBase, null, r);
        }

        Personagem p = (Personagem) atacante;
        System.out.println("Escolha sua Ação:");
        System.out.println("1. Ataque Físico Direto");
        System.out.println("2. Usar Magia / Ataque Elemental");
        System.out.println("3. Usar Buff Temporário (ex: Poção de Força/Vida)");
        int escolha = InputUtil.readInt("Opção: ", 1, 3);

        if (escolha == 1) {
            Equipamento arma = p.getEquipamentosEquipados().get(SlotType.MAO_PRINCIPAL);
            if (arma == null) {
                arma = p.getEquipamentosEquipados().get(SlotType.DUAS_MAOS);
            }

            double danoBase;
            if (arma != null) {
                String tipoMunicao = arma.getTipoMunicao();
                int qtdMunicao = arma.getQuantidadeMunicao() != null ? arma.getQuantidadeMunicao() : 0;

                if (tipoMunicao != null && !tipoMunicao.isEmpty() && qtdMunicao > 0) {
                    boolean temMunicao = consumirMunicaoQuantidade(p.getInventario(), tipoMunicao, qtdMunicao);
                    if (!temMunicao) {
                        System.out.printf("❌ Sem munição '%s' (%d unidades) na mochila para usar %s!\n", tipoMunicao, qtdMunicao, arma.getNome());
                        System.out.println("Você realiza um soco básico desarmado no lugar.");
                        danoBase = p.getStatusFinalAtributo("forca") + 5.0;
                        return processarAtaqueSimulado(p.getNome(), defensor, hpDefensor, danoBase, null, r);
                    }
                    System.out.printf("🏹 Consumido %dx '%s' da mochila.\n", qtdMunicao, tipoMunicao);
                }

                danoBase = p.getStatusFinalAtributo("forca") + (arma.getDano() != null ? arma.getDano() : 0.0) + 10.0;
                System.out.printf("⚔️ Atacando com %s (Dano Arma: %.1f)\n", arma.getNome(), arma.getDano());
            } else {
                danoBase = p.getStatusFinalAtributo("forca") + 10.0; // Ataque desarmado
                System.out.println("👊 Atacando de mãos vazias.");
            }

            return processarAtaqueSimulado(p.getNome(), defensor, hpDefensor, danoBase, null, r);
        } else if (escolha == 2) {
            if (p.getMagias().isEmpty()) {
                UiFormatter.printWarning("Você não conhece magias! Usando ataque físico no lugar.");
                double danoBase = p.getStatusFinalAtributo("forca") + 15.0;
                return processarAtaqueSimulado(p.getNome(), defensor, hpDefensor, danoBase, null, r);
            } else {
                System.out.println("Selecione a Magia:");
                for (int i = 0; i < p.getMagias().size(); i++) {
                    System.out.printf("%d. %s (%s)\n", i + 1, p.getMagias().get(i).getNome(), p.getMagias().get(i).getNomeTraduzido());
                }
                int idxM = InputUtil.readInt("Magia: ", 1, p.getMagias().size());
                Magia m = p.getMagias().get(idxM - 1);
                double danoBase = p.getStatusFinalAtributo("inteligencia") * 1.5 + 20.0;
                return processarAtaqueSimulado(p.getNome(), defensor, hpDefensor, danoBase, m, r);
            }
        } else {
            System.out.println("Escolha o Buff:");
            System.out.println("1. Poção de Fúria (+5 Força, 3 turnos)");
            System.out.println("2. Poção de Haste (+5 Velocidade, 3 turnos)");
            System.out.println("3. Poção de Defesa (+25 Vida/HP extra temporário, 2 turnos)");
            int idxB = InputUtil.readInt("Opção: ", 1, 3);
            EfeitoTemporario eff;
            if (idxB == 1) {
                eff = new EfeitoTemporario("Poção de Fúria", "forca", 5.0, 3);
            } else if (idxB == 2) {
                eff = new EfeitoTemporario("Poção de Haste", "velocidade", 5.0, 3);
            } else {
                eff = new EfeitoTemporario("Poção de Defesa", "vida", 25.0, 2);
            }
            p.getEfeitosTemporarios().add(eff);
            StatusManager.recalcularStatus(p);
            UiFormatter.printSuccess("Aplicou: " + eff.getNome() + "! Status recalculados!");
            return hpDefensor;
        }
    }

    private double processarAtaqueSimulado(String atacanteNome, EntidadeRPG defensor, double hpDefensor, double danoBase, Magia m, Random r) {
        double destrezaDefensor = defensor.getStatusFinalAtributo("destreza");
        double esquivaChance = Math.min(40.0, destrezaDefensor * 2.0); // max 40% de esquiva

        int rolagem = r.nextInt(100) + 1;
        if (rolagem <= esquivaChance) {
            UiFormatter.printWarning("O golpe ERROU! " + defensor.getNome() + " se esquivou!");
            return hpDefensor;
        }

        double danoFinal = danoBase;
        if (m != null) {
            danoFinal = CombatManager.calcularDano(defensor, m, danoBase);
            if (danoFinal > danoBase) {
                UiFormatter.printWarning("VULNERABILIDADE ELEMENTAR DETECTADA! Dano ampliado em 50%!");
            }
        }

        double novoHp = Math.max(0.0, hpDefensor - danoFinal);
        System.out.printf("💥 Dano Infligido em %s: %.1f HP. Novo HP: %.1f\n", defensor.getNome(), danoFinal, novoHp);
        return novoHp;
    }

    private void menuMercado() {
        Personagem p = selecionarPersonagem();
        if (p == null) return;

        while (true) {
            MenuBuilder menu = new MenuBuilder("🛒 MERCADO IMPERIAL - " + p.getNome().toUpperCase());
            menu.addItem(1, "Comprar Item do Catálogo", "Comprar novos itens")
                .addItem(2, "Vender Item da Mochila", "Vender para o mercado por 50%")
                .addItem(3, "Exibir Catálogo Geral", "Ver itens disponíveis")
                .addItem(0, "Voltar", "Menu anterior");

            int opcao = menu.getUserChoice(0, 3);
            switch (opcao) {
                case 1:
                    comprarItemLoja(p);
                    break;
                case 2:
                    venderItemLoja(p);
                    break;
                case 3:
                    exibirCatalogoLoja();
                    break;
                case 0:
                    return;
            }
        }
    }

    private void comprarItemLoja(Personagem p) {
        List<Item> catalogo = ItemFactory.listarCatalogo();
        if (catalogo.isEmpty()) {
            UiFormatter.printWarning("O catálogo global está vazio. Importe itens primeiro!");
            return;
        }

        UiFormatter.printSubtitle("🛍️ COMPRAR ITEM");
        System.out.println("Saldo Atual: G$ " + p.getInventario().getDinheiroG() + " / " + p.getInventario().getMaxG() + " (Teto)");
        for (int i = 0; i < catalogo.size(); i++) {
            Item item = catalogo.get(i);
            double precoCompra = EconomyManager.calcularPrecoCompra(item, p);
            System.out.printf("%d. %s - Preço Base: G$ %.2f | Com Carisma: G$ %.2f (%s)\n", 
                i + 1, item.getNome(), item.getValorComercial(), precoCompra, item.getDescricao());
        }
        System.out.println("0. Voltar");
        int index = InputUtil.readInt("Opção: ", 0, catalogo.size());
        if (index == 0) return;

        Item itemEscolhido = catalogo.get(index - 1);
        double precoFinal = EconomyManager.calcularPrecoCompra(itemEscolhido, p);
        if (p.getInventario().getItens().size() >= p.getInventario().getMaxItens()) {
            UiFormatter.printError("Erro: Mochila cheia!");
            return;
        }
        if (p.getInventario().getDinheiroG() < precoFinal) {
            UiFormatter.printError("Erro: G$ insuficiente!");
            return;
        }

        if (EconomyManager.comprarItem(p, itemEscolhido)) {
            repository.salvar(p);
            UiFormatter.printSuccess("Item '" + itemEscolhido.getNome() + "' comprado com sucesso por G$ " + precoFinal + "! 🎒");
        } else {
            UiFormatter.printError("Erro ao processar transação.");
        }
    }

    private void venderItemLoja(Personagem p) {
        List<Item> itens = p.getInventario().getItens();
        if (itens.isEmpty()) {
            UiFormatter.printWarning("Sua mochila está vazia!");
            return;
        }

        UiFormatter.printSubtitle("💰 VENDER ITEM (50% do Preço Base)");
        for (int i = 0; i < itens.size(); i++) {
            Item item = itens.get(i);
            double precoVenda = EconomyManager.calcularPrecoVenda(item, p);
            System.out.printf("%d. %s (Venda: G$ %.2f) - %s\n", i + 1, item.getNome(), precoVenda, item.getDescricao());
        }
        System.out.println("0. Voltar");
        int index = InputUtil.readInt("Opção: ", 0, itens.size());
        if (index == 0) return;

        Item itemVenda = itens.get(index - 1);
        double precoVendaFinal = EconomyManager.calcularPrecoVenda(itemVenda, p);
        if (p.getInventario().getDinheiroG() + precoVendaFinal > p.getInventario().getMaxG()) {
            UiFormatter.printWarning("Atenção: Adicionar G$ " + precoVendaFinal + " excederá o teto da sua mochila (" + p.getInventario().getMaxG() + "). O excedente será descartado.");
        }

        if (EconomyManager.venderItem(p, itemVenda)) {
            repository.salvar(p);
            UiFormatter.printSuccess("Item '" + itemVenda.getNome() + "' vendido com sucesso por G$ " + precoVendaFinal + "! 🪙");
        } else {
            UiFormatter.printError("Erro ao vender o item.");
        }
    }

    private void exibirCatalogoLoja() {
        List<Item> catalogo = ItemFactory.listarCatalogo();
        if (catalogo.isEmpty()) {
            UiFormatter.printWarning("Nenhum item cadastrado no catálogo.");
            return;
        }
        UiFormatter.printSubtitle("📖 CATÁLOGO DE ITENS DISPONÍVEIS");
        for (Item item : catalogo) {
            String tipo = (item instanceof Equipamento) ? "EQUIPAMENTO (" + ((Equipamento) item).getSlotCompativel() + ")" : "CONSUMÍVEL";
            System.out.printf("  * %s (%s) - Valor Base: G$ %.2f - %s\n", 
                item.getNome(), tipo, item.getValorComercial(), item.getDescricao());
        }
        InputUtil.pressEnterToContinue();
    }

    private void menuImportacaoItens() {
        while (true) {
            MenuBuilder menu = new MenuBuilder("🛠️ IMPORTAÇÃO & CADASTRO DE ITENS 🛠️");
            menu.addItem(1, "Criar Item e Adicionar ao Catálogo", "Formulário manual")
                .addItem(2, "Importar Itens em Lote (CSV)", "Ler arquivos da pasta dados/imports")
                .addItem(3, "Gerenciar Catálogo", "Visualizar, editar ou excluir itens do catálogo")
                .addItem(4, "Visualizar Todos os Itens (Detalhado)", "Exibir detalhes de todos os itens cadastrados")
                .addItem(0, "Voltar", "Menu anterior");

            int opcao = menu.getUserChoice(0, 4);
            switch (opcao) {
                case 1:
                    criarItemManualCatalogo();
                    break;
                case 2:
                    importarLoteCSV();
                    break;
                case 3:
                    gerenciarCatalogoItens();
                    break;
                case 4:
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
        double valor = InputUtil.readDouble("Valor Comercial Base: ", 0, Double.MAX_VALUE);

        System.out.println("Tipo do Item:");
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

            novoItem = new Equipamento(nome, desc, valor, "G$", slot, mods, habs);
        } else {
            int cargas = InputUtil.readInt("Cargas / Usos: ", 1, 100);
            novoItem = new ItemConsumivel(nome, desc, valor, "G$", cargas);
        }

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
}
