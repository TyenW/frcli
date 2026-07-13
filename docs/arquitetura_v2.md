# Especificações e Funcionamento da RPG Engine V2

Este documento detalha o funcionamento técnico, diagramas de classe e fluxos operacionais implementados na Versão 2 (V2) da RPG Engine do FRCLI.

---

## 1. Barramento de Eventos (Event-Driven Architecture)

O sistema utiliza um barramento centralizado Pub/Sub para garantir o desacoplamento de managers e automação reativa em tempo real.

*   **Barramento Central (EventBus):** Implementado como um Singleton thread-safe que gerencia inscrições dinâmicas de listeners por classe de evento.
*   **Tipos de Eventos:**
    *   `RpgEvent`: Classe mãe contendo timestamp.
    *   `CombateIniciadoEvent`: Disparado no início do combate.
    *   `TurnoFinalizadoEvent`: Disparado a cada rodada de combate finalizada.
    *   `EntidadeDerrotadaEvent`: Disparado quando o HP de qualquer combatente chega a zero.
    *   `ItemEquipadoEvent` e `ItemDesequipadoEvent`: Emitidos ao alterar equipamentos.

### Logs de Auditoria
O `AuditManager` subscreve-se a todo o barramento, gravando logs operacionais estruturados com data e hora local em:
📁 `dados/historico_campanha.log`

---

## 2. Expansão Polimórfica de Entidades

O sistema separa dados de personagens de dados de NPCs/Monstros, reduzindo a complexidade em memória.

*   `EntidadeRPG`: Base abstrata com statusFinal e nome comuns.
*   `Personagem`: Ficha completa do jogador, incluindo mochila, magias, e buffs temporários.
*   `Monstro`: Ficha leve para monstros carregados dinamicamente do catálogo global `config/monstros.json`, contendo tabela de drop e lista de ataques.

---

## 3. Gestão de Buffs Temporários (TTL)

*   `EfeitoTemporario`: Buffs e debuffs (ex: `Poção de Fúria` adiciona `+5.0` em `forca` por `3` turnos).
*   **Decaimento Automático:** `StatusManager` escuta o `TurnoFinalizadoEvent` decrementando o TTL dos efeitos. Ao expirar (TTL = 0), o buff é removido e os status finais do jogador são recalculados na hora.

---

## 4. Fábrica de Itens e Importador CSV

*   `ItemFactory`:
    *   Centraliza o catálogo de itens em `config/itens_db.json`.
    *   Clonagem profunda segura de objetos utilizando serialização Jackson.
    *   **Importação em Lote:** Lê arquivos `.csv` posicionados sob `/dados/imports` e os converte nativamente para o catálogo de Equipamentos ou Consumíveis.

**Formato Esperado no CSV:**
`Nome;Descrição;PreçoBase;EQUIPAMENTO;Slot;Modificadores(Atributo:Valor);Habilidades(Nome:Tipo:Descrição)`

---

## 5. Loot Procedural

*   `LootManager`: Ao derrotar um monstro (vida <= 0), o sistema calcula os drop-rates com base na tabela ponderada do monstro:
    *   Drops de Ouro G$
    *   Drops de itens que são clonados do catálogo global.
    *   Interface do terminal permite que o jogador decida se quer recolher o saque ou descartar.

---

## 6. Sistema de Comércio (Mercado)

*   `EconomyManager`: Executa compras e vendas de forma atômica protegida contra erros.
    *   **Preço de Compra:** O valor comercial sofre desconto direto com base no Carisma final do jogador (Preço Final = Preço Base * (1.0 - (Carisma / 50.0)), até 50% de desconto máximo).
    *   **Preço de Venda:** Itens da mochila podem ser vendidos ao mercado por **50%** de seu valor comercial base.
    *   **Controle de Carteira:** Impede a venda se o ouro ultrapassar o teto financeiro (`maxG`) configurado no inventário (mochila).
