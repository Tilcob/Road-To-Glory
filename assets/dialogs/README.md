# Dialog Yarn conventions

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

### Example: idle + root

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

## Effects (commands inside choices)

Dialog effects are intentionally limited to quest and flag state changes. Effects are parsed from command lines inside a choice body

- `ADD_QUEST`
- `SET_FLAG`
- `QUEST_STEP`

Rewards (currency, items, experience, etc.) **must not** be applied through dialog effects. If dialog-triggered rewards become necessary in the future, add a **dedicated dialog event type** that is handled by a reward-specific system instead of mutating economy state directly from dialog processing.

### Example: choices + node jumps

```yarn
title: start
tags: root
---
-> Show me your wares.
    <<jump shop_wares>>
===

title: shop_wares
---
I'm still setting up. Come back soon!
===
```

### Example: node-local choices

```yarn
title: start
tags: root
---
-> I'm just passing through.
    <<jump warning>>
===

title: warning
---
Keep the peace.
-> Understood.
    Move along.
===
```

### Example: SET_FLAG

```yarn
title: start
tags: root
---
-> Show me your wares.
    <<set_flag shop_seen true>>
    <<jump shop_wares>>
===
```

### Example: ADD_QUEST

```yarn
title: start
tags: root
---
-> Any work for me?
    <<add_quest Welcome_To_Town>>
===
```

### Example: QUEST_STEP

```yarn
title: start
tags: root
---
-> I spoke with the shopkeeper.
    <<quest_step TALK Npc-2>>
===
```
