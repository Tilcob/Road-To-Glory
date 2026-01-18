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

### Quest and flag dialogs

Quest and flag dialog lines are derived from tags on nodes:

- Quest tags use the pattern `quest_<QuestId>` plus a status tag:
    - `notstarted` → `QuestDialog.notStarted`
    - `inprogress` → `QuestDialog.inProgress`
    - `completed` → `QuestDialog.completed`
- Optional step-specific dialogs use `quest_<QuestId>` plus `step_<index>` (0-based index).
- Flag dialogs use the pattern `flag_<flagName>` and populate `DialogData.flagDialogs`.

Nodes with quest or flag tags are not added to `DialogData.nodes`.

### File name → NPC mapping

Each `.yarn` file produces a single `DialogData` entry keyed by the file name (without extension). For example, `Shopkeeper.yarn` creates the dialog entry `Shopkeeper`.

NPC ids follow the same convention as file names. An NPC id like `Npc-2` resolves to
`dialogs/Npc-2.yarn` when using the directory listing or default file resolution.

If you need a different file name, supply an alias map when constructing the dialog
repository so `npcId` can point to a different `*.yarn` file.

### Dialog discovery

Dialogs can be loaded by listing the `dialogs/` directory or by using a manifest file at
`dialogs/index.json`.

#### Manifest (`dialogs/index.json`)

The manifest supports two formats:

- Array: a list of file names or paths. NPC ids are inferred from the file name.
- Object: explicit NPC id → file name/path mapping (useful for aliases).

```json
[
  "Npc-2.yarn",
  "vendors/Shopkeeper.yarn"
]
```

```json
{
  "Npc-2": "Npc-2.yarn",
  "Mayor": "town/MayorDialog.yarn"
}
```

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
### Example: quest dialog tags

```yarn
title: quest_Welcome_To_Town_notStarted
tags: quest_Welcome_To_Town, notstarted
---
Welcome in our town!
===

title: quest_Welcome_To_Town_inProgress
tags: quest_Welcome_To_Town, inprogress
---
Are you new here?
===

title: quest_Welcome_To_Town_completed
tags: quest_Welcome_To_Town, completed
---
By my friend!
===
```

### Example: flag dialog tags

```yarn
title: flag_shop_seen
tags: flag_shop_seen
---
Good to see you again.
===
```
