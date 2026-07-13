# Atributos e Status Base

Este documento define os atributos e o sistema de modificadores do motor de RPG do sistema.

## 1. Atributos Base Padronizados
Todos os personagens do jogo começam com os seguintes atributos antes de aplicar qualquer bônus ou penalidade:

* **Vida Base:** `120.0` (valor fixo para todos)
* **Demais Atributos:** `0.0` por padrão (Força, Velocidade, Inteligência, Destreza, Intelectualidade, Carisma, Cura)

---

## 2. Tabela de Modificadores (Bônus e Penalidades)
Quando uma Raça, Classe ou Magia ativa aplica um modificador positivo `(+)` ou negativo `(-)`, o motor Java utiliza os seguintes valores para somar ou subtrair do atributo correspondente:

| Atributo | Modificador Positivo `(+)` | Modificador Negativo `(-)` |
| :--- | :---: | :---: |
| **Vida (HP)** | `+75.0` | `-35.0` |
| **Força** | `+3.0` | `-0.5` |
| **Velocidade** | `+3.0` | `-0.5` |
| **Inteligência** | `+2.0` | `-1.0` |
| **Destreza** | `+2.0` | `-1.0` |
| **Intelectualidade** | `+4.0` | `-1.5` |
| **Carisma** | `+4.0` | `-1.5` |
| **Cura** | `+10.0` | `-5.0` |

> [!NOTE]
> **Integração do Atributo Defesa:**
> O atributo `defesa` foi unificado ao atributo `vida`. Todos os modificadores de `defesa` nas tabelas do jogo são automaticamente convertidos e aplicados à `vida` (`+75.0` para positivo, `-35.0` para negativo).

---

## 3. Pipeline de Cálculo de Status Final
Os status finais exibidos na ficha detalhada do personagem são calculados dinamicamente em tempo real usando a fórmula:

$$\text{Status Final} = \text{Status Base} + \text{Modificador de Raça} + \text{Modificador de Classe} + \text{Modificador de Magias} + \text{Modificadores de Equipamentos} + \text{Buffs Temporários}$$
