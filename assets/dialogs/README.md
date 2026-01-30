# Dialog Yarn conventions

Yarn dialog files are parsed with the **Yarn Spinner Java** format (dependency `org.yarnspinner:yarnspinner:2.4.0`). The loader expects the standard header/body separators:

- Header fields like `title:` and optional `tags:`
- `---` marks the start of the body
- `===` ends a node

## Node → `DialogData` mapping

- `title:` becomes `DialogNode.id`
- Body lines (non-command lines) become `DialogNode.lines`
- `-> Option text` becomes a `DialogChoice` with `text = "Option text"`
- Indented lines under an option become `DialogChoice.lines`
- `<<jump NodeId>>` or `<<goto NodeId>>` inside an option sets `DialogChoice.next`

## Entry points and tags

Use tags to decide how nodes populate the top-level dialog fields:

- Tag `idle` supplies `DialogData.idle`
- Tag `root` or `start` supplies `DialogData.choices`
- All other nodes are added to `DialogData.nodes`

If no tagged root exists, the loader falls back to the node named `Start`.

## Quest and flag dialogs

Quest and flag dialog lines are derived from tags on nodes:

- Quest tags use the pattern `quest_<QuestId>` plus a status tag:
    - `notstarted` → `QuestDialog.notStarted`
    - `inprogress` → `QuestDialog.inProgress`
    - `completed` → `QuestDialog.completed`
- Optional step-specific dialogs use `quest_<QuestId>` plus `step_<index>` (0-based index).
- Flag dialogs use the pattern `flag_<flagName>` and populate `DialogData.flagDialogs`.

Nodes with quest or flag tags are not added to `DialogData.nodes`.

To show a quest `completed` dialog only once, set the dialog flag
`quest_<questId>_completed_seen` (for example, via `<<set_flag quest_<questId>_completed_seen true>>`).
When the flag is set, completed dialogs fall back to idle/root lines.

## File name → NPC mapping

Each `.yarn` file produces a single `DialogData` entry keyed by the file name (without extension). For example, `Shopkeeper.yarn` creates the dialog entry `Shopkeeper`.

NPC ids follow the same convention as file names. An NPC id like `Npc-2` resolves to
`dialogs/Npc-2.yarn` when using the directory listing or default file resolution.

If you need a different file name, supply an alias map when constructing the dialog
repository so `npcId` can point to a different `*.yarn` file.

## Dialog discovery

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

## Custom Yarn commands

The project defines additional Yarn commands that are interpreted by the dialog loader (effects)
or by the quest Yarn runtime (command lines in quest nodes). Use the exact casing shown below.

### Conditional logic

Dialog Yarn supports conditional blocks like:
```yarn
<<if <expression>>>
Text when condition is true
<<else>>
Text when condition is false
<<endif>>
```

### Rules

- `<<if ...>>` works both inside and outside choices
- Outside of choices, `<<if ...>>` is used only for text filtering
- Conditional blocks are evaluated at compile time
- Lines that do not match the condition are not added to the dialog

### Expression functions

Functions may only be used inside expressions (for example, in `<<if ...>>`).
Standalone calls like ``<<has_item "potion">>``, are not supported and will be treated as commands.

### Available functions

All functions are read-only and evaluated against the current player state.
- `flag("<name>")` returns `bool`
- `counter("<name>")` returns `int`
- `has_item("<itemId>")` returns `bool`
- `quest_is_active("<questId>")` returns `bool`
- `quest_is_completed("<questId>")` returns `bool`
- `quest_stage("<questId>")` returns `int`

### Dialog choice effects (parsed by the dialog loader)

These commands are only recognized when they appear inside an indented choice body.

- `<<set_flag <flag> <true|false>>>`: Set a dialog flag (boolean).
- `<<add_quest <questId>>>`: Add a quest to the player's quest log.
- `<<quest_step <type> <target>>>`: Trigger a quest step event.

### Dialog runtime commands (command lines inside dialog nodes)

Direct reward commands are only supported in dialog yarn nodes (never in quest yarn).

- `<<give_money <amount>>>`: Grant currency to the player.
- `<<give_item <itemId> <amount>>>`: Grant one or more items (amount defaults to `1`).

### Example: outside a choice <<if ...>>

```yarn
<<if has_item("potion")>>
You look well prepared.
<<else>>
You seem unprepared.
<<endif>>
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
