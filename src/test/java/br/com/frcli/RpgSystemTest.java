package br.com.frcli;

import br.com.frcli.manager.*;
import br.com.frcli.model.*;
import br.com.frcli.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RpgSystemTest {

    private Personagem personagem;
    private Raca racaHumano;
    private Raca racaElfo;
    private Classe classeMago;
    private Magia magiaPyromancia;
    private Magia magiaHydromacia;

    @BeforeEach
    public void setup() {
        // Setup Raças
        racaHumano = new Raca("Humano",
                new Raca.ModificadoresInfo(Arrays.asList("+ velocidade", "+ intelectualidade"), Collections.singletonList("Confiança")),
                new Raca.ModificadoresInfo(Collections.singletonList("- carisma"), Collections.singletonList("Dificuldade com necromancia"))
        );

        racaElfo = new Raca("Elfo",
                new Raca.ModificadoresInfo(Arrays.asList("+ velocidade", "+ destreza"), Collections.singletonList("Furtividade")),
                new Raca.ModificadoresInfo(Collections.singletonList("- força"), Collections.singletonList("Pequeno"))
        );

        // Setup Classe
        // Mago: + força, + carisma, - velocidade, - intelectualidade
        Classe.BalanceamentoStatus statusMago = new Classe.BalanceamentoStatus(
                Arrays.asList("+ força", "+ carisma"),
                Arrays.asList("- velocidade", "- intelectualidade")
        );
        classeMago = new Classe("Mago", "Magias Elementares", statusMago, "grande", new HashMap<>());
        
        // Habilidade de Classe por Tier
        Habilidade firebolt = new Habilidade("Firebolt", "ATIVA", "Dispara fogo");
        classeMago.adicionarHabilidadeAoTier(1, firebolt);
        Habilidade teleport = new Habilidade("Teletransporte", "ATIVA", "Teleporta curto alcance");
        classeMago.adicionarHabilidadeAoTier(2, teleport);

        // Setup Magias
        // Pyromacia: + força, + carisma, - resistencia a hydromacia
        magiaPyromancia = new Magia("Pyromacia", "Magia de Fogo",
                new Magia.ModificadoresMagia(Arrays.asList("+ força", "+ carisma"), Collections.singletonList("- resistencia a hydromacia")),
                "Dano de fogo"
        );

        magiaHydromacia = new Magia("Hydromacia", "Magia de Água",
                new Magia.ModificadoresMagia(Arrays.asList("+ velocidade"), Collections.singletonList("- resistencia a geomacia")),
                "Dano de água"
        );

        // Setup Personagem
        personagem = new Personagem();
        personagem.setNome("Geraldo");
        personagem.setRaca(racaHumano);
        personagem.setClasse(classeMago);
        personagem.setTierCampanha(1);

        // Status base
        Map<String, Double> base = new HashMap<>();
        base.put("vida", 120.0);
        base.put("forca", 10.0);
        base.put("velocidade", 10.0);
        base.put("destreza", 5.0);
        base.put("carisma", 8.0);
        personagem.setStatusBase(base);

        // Mochila
        Mochila mochila = new Mochila(MochilaType.PEQUENA, new ArrayList<>(), 100.0);
        personagem.setInventario(mochila);

        // Calcula status iniciais
        StatusManager.recalcularStatus(personagem);
    }

    @Test
    public void testStatusCalculationPipeline() {
        // Inicial:
        // Base: HP=120, Força=10, Vel=10, Destreza=5, Carisma=8
        // Humano: + velocidade (+3), + intelectualidade (+4), - carisma (-1.5)
        // Mago: + força (+3), + carisma (+4), - velocidade (-0.5), - intelectualidade (-1.5)
        // Final esperado:
        // HP = 120
        // Força = 10 + 3 (Mago) = 13.0
        // Velocidade = 10 + 3 (Humano) - 0.5 (Mago) = 12.5
        // Carisma = 8 - 1.5 (Humano) + 4 (Mago) = 10.5
        // Intelectualidade = 0 + 4 (Humano) - 1.5 (Mago) = 2.5
        
        assertEquals(120.0, personagem.getStatusFinalAtributo("vida"));
        assertEquals(13.0, personagem.getStatusFinalAtributo("forca"));
        assertEquals(12.5, personagem.getStatusFinalAtributo("velocidade"));
        assertEquals(10.5, personagem.getStatusFinalAtributo("carisma"));
        assertEquals(2.5,  personagem.getStatusFinalAtributo("intelectualidade"));

        // Equipar anel: +20 Vida, -2 Destreza
        Map<String, Double> modsAnel = new HashMap<>();
        modsAnel.put("Vida", 20.0);
        modsAnel.put("Destreza", -2.0);
        Equipamento anel = new Equipamento("Anel da Vida", "Dá vida extra", 150.0, "G$",
                SlotType.ACESSORIO, modsAnel, new ArrayList<>());

        EquipmentManager.equipar(personagem, anel, SlotType.ACESSORIO_1);

        // Vida deve ir para 140.0, Destreza para 3.0 (Base 5.0)
        assertEquals(140.0, personagem.getStatusFinalAtributo("vida"));
        assertEquals(3.0, personagem.getStatusFinalAtributo("destreza"));

        // Desequipar
        EquipmentManager.desequipar(personagem, SlotType.ACESSORIO_1);

        // Volta ao original
        assertEquals(120.0, personagem.getStatusFinalAtributo("vida"));
        assertEquals(5.0, personagem.getStatusFinalAtributo("destreza"));
    }

    @Test
    public void testInventoryCapacity() {
        Mochila mochila = personagem.getInventario();
        assertEquals(15, mochila.getMaxItens());

        // Adiciona 15 itens consumíveis
        for (int i = 0; i < 15; i++) {
            Item potion = new ItemConsumivel("Poção " + i, "Cura leve", 10.0, "G$", 1);
            InventoryManager.adicionarItem(mochila, potion);
        }

        assertEquals(15, mochila.getItens().size());

        // 16º item deve lançar exceção
        Item potion16 = new ItemConsumivel("Poção Extra", "Cura leve", 10.0, "G$", 1);
        assertThrows(MochilaCheiaException.class, () -> {
            InventoryManager.adicionarItem(mochila, potion16);
        });
    }

    @Test
    public void testGWalletLimits() {
        Mochila mochila = personagem.getInventario();
        assertEquals(25000.0, mochila.getMaxG());

        // Adiciona 20.000 G$
        InventoryManager.adicionarG(mochila, 20000.0, true);
        assertEquals(20000.0, mochila.getDinheiroG());

        // Adicionar + 10.000 G$ com erro ativado deve falhar
        assertThrows(CarteiraCheiaException.class, () -> {
            InventoryManager.adicionarG(mochila, 10000.0, true);
        });

        // Adicionar + 10.000 G$ com erro desativado deve limitar a 25.000 G$ e descartar o resto
        InventoryManager.adicionarG(mochila, 10000.0, false);
        assertEquals(25000.0, mochila.getDinheiroG());
    }

    @Test
    public void testTwoHandedWeaponSlots() {
        // Equipamentos iniciais
        Equipamento espada1M = new Equipamento("Espada Curta", "Uma mão", 50.0, "G$",
                SlotType.MAO_PRINCIPAL, new HashMap<>(), new ArrayList<>());
        
        Equipamento escudo = new Equipamento("Escudo", "Defesa", 30.0, "G$",
                SlotType.MAO_SECUNDARIA, new HashMap<>(), new ArrayList<>());

        EquipmentManager.equipar(personagem, espada1M, SlotType.MAO_PRINCIPAL);
        EquipmentManager.equipar(personagem, escudo, SlotType.MAO_SECUNDARIA);

        assertEquals(espada1M, personagem.getEquipamentosEquipados().get(SlotType.MAO_PRINCIPAL));
        assertEquals(escudo, personagem.getEquipamentosEquipados().get(SlotType.MAO_SECUNDARIA));

        // Equipar arma de duas mãos
        Equipamento cajado2M = new Equipamento("Cajado Elemental", "Duas mãos", 300.0, "G$",
                SlotType.DUAS_MAOS, new HashMap<>(), new ArrayList<>());

        List<Equipamento> desequipados = EquipmentManager.equipar(personagem, cajado2M, SlotType.DUAS_MAOS);

        // Cajado deve estar em ambos os slots de mão
        assertEquals(cajado2M, personagem.getEquipamentosEquipados().get(SlotType.MAO_PRINCIPAL));
        assertEquals(cajado2M, personagem.getEquipamentosEquipados().get(SlotType.MAO_SECUNDARIA));

        // Espada e escudo devem ter sido desequipados
        assertTrue(desequipados.contains(espada1M));
        assertTrue(desequipados.contains(escudo));

        // Agora se equipar o escudo novamente na mão secundária, o cajado de duas mãos deve desequipar de ambas as mãos
        List<Equipamento> desequipadosEscudo = EquipmentManager.equipar(personagem, escudo, SlotType.MAO_SECUNDARIA);
        assertNull(personagem.getEquipamentosEquipados().get(SlotType.MAO_PRINCIPAL));
        assertEquals(escudo, personagem.getEquipamentosEquipados().get(SlotType.MAO_SECUNDARIA));
        assertTrue(desequipadosEscudo.contains(cajado2M));
    }

    @Test
    public void testCombatAndElementalWeakness() {
        // Personagem ganha Pyromacia
        personagem.getMagias().add(magiaPyromancia);
        StatusManager.recalcularStatus(personagem);

        // Se sofrer ataque de Hydromacia (que o defensor é vulnerável)
        double danoFinal = CombatManager.calcularDano(personagem, magiaHydromacia, 100.0);
        // Esperado: 100.0 * 1.5 = 150.0
        assertEquals(150.0, danoFinal);

        // Se sofrer ataque físico (nulo) ou de Pyromacia (sem fraqueza listada no defensor)
        double danoNormal = CombatManager.calcularDano(personagem, magiaPyromancia, 100.0);
        assertEquals(100.0, danoNormal);
    }

    @Test
    public void testHibridoRaceModifiers() {
        // Híbrido de Humano e Elfo
        Raca hibrido = new Raca("Híbrido", new Raca.ModificadoresInfo(new ArrayList<>(), new ArrayList<>()), new Raca.ModificadoresInfo(new ArrayList<>(), new ArrayList<>()));
        personagem.setRaca(hibrido);
        personagem.getSubRacas().addAll(Arrays.asList(racaHumano, racaElfo));

        StatusManager.recalcularStatus(personagem);

        // Humano vantagens: ["+ velocidade", "+ intelectualidade"]
        // Elfo vantagens: ["+ velocidade", "+ destreza"]
        // Híbrido vantagens resolve: vantagens do 1º (+ velocidade e + intelectualidade) e a primeira vantagem do 2º (+ velocidade)
        // Delta de Velocidade total das vantagens do Híbrido: +3 (Humano) +3 (Elfo) = +6
        // Delta Intelectualidade: +4 (Humano)
        
        // Humano desvantagens: ["- carisma"]
        // Elfo desvantagens: ["- força"] (wait, desvantagens.modificadores de Elfo is "- força", Humano is "- carisma")
        // Híbrido desvantagens resolve: desvantagens do 2º (Elfo: - força) e a primeira do 1º (Humano: - carisma)
        // Delta de Força: -0.5 (Elfo)
        // Delta de Carisma: -1.5 (Humano)
        
        // Base: HP=120, Força=10, Vel=10, Destreza=5, Carisma=8
        // Mago balanceamento: + força (+3), + carisma (+4), - velocidade (-0.5), - intelectualidade (-1.5)
        
        // Velocidade final = 10 + 6 (Raça) - 0.5 (Mago) = 15.5
        // Força final = 10 - 0.5 (Raça) + 3 (Mago) = 12.5
        
        assertEquals(15.5, personagem.getStatusFinalAtributo("velocidade"));
        assertEquals(12.5, personagem.getStatusFinalAtributo("forca"));
    }

    @Test
    public void testClassTierAbilitiesUnlocking() {
        // No Tier 1: apenas habilidade "Firebolt"
        List<Habilidade> habilidadesT1 = CombatManager.obterAcoesDisponiveis(personagem);
        assertEquals(1, habilidadesT1.size());
        assertEquals("Firebolt", habilidadesT1.get(0).getNome());

        // Sobe para o Tier 2: habilidade "Firebolt" + "Teletransporte"
        personagem.setTierCampanha(2);
        List<Habilidade> habilidadesT2 = CombatManager.obterAcoesDisponiveis(personagem);
        assertEquals(2, habilidadesT2.size());
        
        // Equipar item com habilidade embutida
        Habilidade curaLeve = new Habilidade("Cura Leve", "ATIVA", "Cura pequeno valor");
        Equipamento anelCura = new Equipamento("Anel Curativo", "Cura", 100.0, "G$", SlotType.ACESSORIO, 
                new HashMap<>(), Collections.singletonList(curaLeve));

        EquipmentManager.equipar(personagem, anelCura, SlotType.ACESSORIO_1);

        // Deve conter Firebolt + Teletransporte + Cura Leve
        List<Habilidade> habilidadesComItem = CombatManager.obterAcoesDisponiveis(personagem);
        assertEquals(3, habilidadesComItem.size());
        assertTrue(habilidadesComItem.stream().anyMatch(h -> h.getNome().equals("Cura Leve")));
    }

    @Test
    public void testJsonSerializationAndDeserialization(@TempDir File tempDir) {
        FichaRepository repo = new JsonFichaRepositoryImpl(tempDir);

        // Adiciona um item na mochila e equipa um elmo
        Equipamento elmo = new Equipamento("Elmo de Ferro", "Proteção", 100.0, "G$", SlotType.CABECA,
                Collections.singletonMap("vida", 10.0), new ArrayList<>());
        
        personagem.getInventario().getItens().add(elmo);
        EquipmentManager.equipar(personagem, elmo, SlotType.CABECA);

        repo.salvar(personagem);

        // Carrega do diretório temporário
        Personagem carregado = repo.buscarPorId(personagem.getNome());
        assertNotNull(carregado);
        assertEquals(personagem.getNome(), carregado.getNome());
        assertEquals(personagem.getClasse().getClasse(), carregado.getClasse().getClasse());
        assertEquals(personagem.getRaca().getRaca(), carregado.getRaca().getRaca());

        // Verifica que o status foi recalculado dinamicamente no carregamento
        // Vida deve conter o bônus do elmo (+10.0) + bônus base
        assertEquals(personagem.getStatusFinalAtributo("vida"), carregado.getStatusFinalAtributo("vida"));
    }

    @Test
    public void testModificadorContagemAndEscritas() {
        personagem.setRaca(racaHumano); // Humano: + velocidade, + intelectualidade, - carisma
        personagem.setClasse(classeMago); // Mago: + força, + carisma, - velocidade, - intelectualidade
        
        StatusManager.recalcularStatus(personagem);
        
        Map<String, AtributoContagem> cont = personagem.getContagemModificadores();
        
        assertEquals(1, cont.get("velocidade").getPositivos());
        assertEquals(1, cont.get("velocidade").getNegativos());
        
        assertEquals(1, cont.get("carisma").getPositivos());
        assertEquals(1, cont.get("carisma").getNegativos());
        
        assertEquals(1, cont.get("forca").getPositivos());
        assertEquals(0, cont.get("forca").getNegativos());
        
        assertTrue(personagem.getVantagensEscritas().contains("Confiança"));
        assertTrue(personagem.getVantagensEscritas().contains("Especialidade da Classe: Magias Elementares"));
        assertTrue(personagem.getDesvantagensEscritas().contains("Dificuldade com necromancia"));
    }
}
