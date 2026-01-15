# Dialog JSON conventions

## Yarn Spinner (.yarn) conventions

Yarn dialog files are parsed with the **Yarn Spinner Java** format (dependency `org.yarnspinner:yarnspinner:2.4.0`). The loader expects the standard header/body separators:

- Header fields like `title:` and optional `tags:`
- `---` marks the start of the body
- `===` ends a node

### Node → `DialogData` mapping

- `title:` becomes `DialogNode.id`
- Body lines (non-command lines) become `DialogNode.lines`
- `-> Option text` becomes a `DialogChoice` with `text = "Option text"`
- Indented lines under an option become `DialogChoice.lines`
- `<<jump NodeId>>` or `<<goto NodeId>>` inside an option sets `DialogChoice.next`

### Entry points and tags

Use tags to decide how nodes populate the top-level dialog fields:

- Tag `idle` supplies `DialogData.idle`
- Tag `root` or `start` supplies `DialogData.choices`
- All other nodes are added to `DialogData.nodes`

If no tagged root exists, the loader falls back to the node named `Start`.

### File name → NPC mapping

Each `.yarn` file produces a single `DialogData` entry keyed by the file name (without extension). For example, `Shopkeeper.yarn` creates the dialog entry `Shopkeeper`.

### Example

```yarn
title: Start
tags: root
---
Hello there.
-> Show me your wares.
    <<jump shop_wares>>
===

title: Idle
tags: idle
---
Welcome in my shop!
===

title: shop_wares
---
I'm still setting up. Come back soon!
===
```

## Effects

Dialog effects are intentionally limited to quest and flag state changes:

- `ADD_QUEST`
- `SET_FLAG`
- `QUEST_STEP`

Rewards (currency, items, experience, etc.) **must not** be applied through dialog effects. If dialog-triggered rewards become necessary in the future, add a **dedicated dialog event type** that is handled by a reward-specific system instead of mutating economy state directly from dialog processing.

## Nodes and choice branching

Dialogs can define named nodes and move to them from choices using a `next` reference. This enables branching without embedding all response lines directly in the choice.

### Defining nodes

Add a `nodes` array to the NPC dialog entry. Each node needs a unique `id` and can define its own `lines` and optional `choices`.

### Linking choices to nodes

Within `choices`, set `next` to the target node ID. When a choice is selected, the dialog system will jump to that node. If `next` is omitted or invalid, the system falls back to the legacy behavior and uses the choice `lines` instead.

### Example

```json
{
  "npcs": {
    "Shopkeeper": {
      "idle": [
        "Welcome in my shop!",
        "Would you like to see my wares?"
      ],
      "choices": [
        {
          "text": "Show me your wares.",
          "next": "shop_wares",
          "lines": [
            "I'm still setting up. Come back soon!"
          ]
        }
      ],
      "nodes": [
        {
          "id": "shop_wares",
          "lines": [
            "I'm still setting up. Come back soon!"
          ]
        }
      ]
    }
  }
}
```

## Example: node-local choices

```json
{
  "npcs": {
    "Guard": {
      "idle": [
        "State your business."
      ],
      "nodes": [
        {
          "id": "warning",
          "lines": [
            "Keep the peace."
          ],
          "choices": [
            {
              "text": "Understood.",
              "lines": [
                "Move along."
              ]
            }
          ]
        }
      ],
      "choices": [
        {
          "text": "I'm just passing through.",
          "next": "warning"
        }
      ]
    }
  }
}
```

## Example: SET_FLAG

```json
{
  "effects": [
    {
      "type": "SET_FLAG",
      "flag": "shop_seen",
      "value": true
    }
  ]
}
```
## Example: ADD_QUEST

```json
{
  "effects": [
    {
      "type": "ADD_QUEST",
      "questId": "Welcome_To_Town"
    }
  ]
}
```

## Example: QUEST_STEP

```json
{
  "effects": [
    {
      "stepType": "TALK",
      "target": "Npc-2"
    }
  ]
}
```
