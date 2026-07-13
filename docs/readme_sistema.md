# Manual do Ecossistema de RPG & Gerenciamento (FRCLI)

Este é o documento mestre que descreve o funcionamento completo, arquitetura técnica e regras de negócios do sistema de gerenciamento de fichas de RPG.

---

## 1. Organização do Repositório

O projeto é estruturado de forma limpa seguindo o padrão Maven:

* **[`/config/`](file:///c:/Users/pedro/Desktop/Codigos/frcli/config)**: Tabelas estáticas globais carregadas em formato JSON (`classes.json`, `raças.json`, `magias.json`, `status.json`).
* **[`/dados/campanha_1/fichas/`](file:///c:/Users/pedro/Desktop/Codigos/frcli/dados/campanha_1/fichas)**: Pasta de persistência onde cada personagem é salvo como um arquivo JSON individual.
* **[`/docs/`](file:///c:/Users/pedro/Desktop/Codigos/frcli/docs)**: Documentação detalhada das regras de negócio do RPG.
* **[`/src/`](file:///c:/Users/pedro/Desktop/Codigos/frcli/src)**: Código-fonte Java do sistema de CLI, gerenciadores, persistência e testes JUnit.

---

## 2. Documentos de Regras do Jogo

Para consultar tabelas, atributos e balanceamentos específicos, acesse os sub-documentos:

1. 📊 **[Atributos e Modificadores (status.md)](file:///c:/Users/pedro/Desktop/Codigos/frcli/docs/status.md)**: Fórmulas de cálculo, valores de modificadores e valores base.
2. ⚔️ **[Classes de Personagem (classe.md)](file:///c:/Users/pedro/Desktop/Codigos/frcli/docs/classe.md)**: Balanceamento, especialidades e tamanho da mochila por classe.
3. 🧬 **[Características das Raças (raças.md)](file:///c:/Users/pedro/Desktop/Codigos/frcli/docs/raças.md)**: Detalhamento de bônus, penalidades e características escritas das raças (e Híbridos).
4. ✨ **[Magias, Poderes e Armas (magias.md)](file:///c:/Users/pedro/Desktop/Codigos/frcli/docs/magias.md)**: Tipos de magia, estilos de combate e resistências elementares.
5. 📂 **[Estrutura da Ficha (ficha.md)](file:///c:/Users/pedro/Desktop/Codigos/frcli/docs/ficha.md)**: Limites de inventário, moedas e diretrizes iniciais da campanha.

---

## 3. Principais Lógicas e Regras do Motor Java

### A. Pipeline Dinâmico de Recálculo de Status
O motor recalcula os status finais em tempo real a cada carregamento ou modificação de ficha. Ele soma os valores base aos modificadores de raça, classe, magias dominadas, itens equipados e buffs temporários.
* **Sem redundância:** O JSON do personagem grava apenas os dados base modificados. A ficha é gerada dinamicamente, mantendo o arquivo de persistência limpo.
* **Valores Padrão:** Vida base padrão é `120.0`. Os demais atributos iniciam em `0.0`.

### B. Contagem de Modificadores e Características Escritas
O sistema compila dinamicamente duas novas estruturas essenciais para a ficha do jogador:
* **Contagem de Modificadores:** Para cada atributo, o sistema analisa todas as vantagens e desvantagens de raça/classe/magia e exibe quantos `+` (pluses) e `-` (minuses) foram aplicados àquele atributo (ex: `Velocidade: (+1, -1)`).
* **Características Escritas:** Agrega em listas de vantagens e desvantagens textuais todos os traços herdados de Raças (incluindo as duas sub-raças de Híbridos), Especialidade de Classes e Observações de Magias aprendidas.

### C. Gerenciador de Equipamentos e Lógica de Duas Mãos
Equipar itens obedece a limites rigorosos nos slots:
* **Slot Tipo Acessório:** Permite equipar até dois acessórios simultâneos (mapeados nos slots `ACESSORIO_1` e `ACESSORIO_2`).
* **Armas de Duas Mãos:** Equipar uma arma pesada (como uma espada colossal) ocupa automaticamente `MAO_PRINCIPAL` e `MAO_SECUNDARIA`. Qualquer item que estivesse equipado nessas mãos é automaticamente devolvido à mochila.

### D. Controle de Mochila e Carteira G$
O inventário restringe peso e valor:
* Adicionar itens ou moedas verifica dinamicamente a capacidade física (Slots) e o teto financeiro de ouro imperial (G$). Ouro ganho acima do limite da mochila é descartado. A moeda padrão do jogo é unificada sob a sigla **G$**.

### E. Arena de Combate e Fraquezas Elementares
Permite duelar entre dois personagens salvos no sistema:
* **Iniciativa:** Rola dado d20 + velocidade final de cada personagem.
* **Esquiva:** Rola contra a destreza final do defensor para determinar se o golpe errou.
* **Dano Físico:** Aplica o dano base diretamente no HP (a antiga mitigação de defesa foi descontinuada após o refactoring).
* **Dano Mágico:** Se o atacante usar magia de um elemento contra o qual o defensor possua fraqueza em suas magias dominadas (ex: Pyromacia vs Hydromacia), o dano sofre ampliação de **+50%** automaticamente.

---

## 4. Refactoring Automático (Defesa -> Vida & Unificação G$)
Toda a base de dados do sistema foi atualizada:
* O atributo `defesa` não existe mais separadamente (aliado a `vida`).
* A moeda `C$` foi extinta. O sistema possui uma rotina de auto-refatoração (migration) tanto para as fichas de personagens salvos (`dados/`) quanto para as tabelas globais (`config/`), substituindo qualquer ocorrência de `defesa` para `vida` e convertendo saldos e valores de `C$` para `G$` de forma 100% transparente na inicialização do jogo.
