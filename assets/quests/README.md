# Quest Yarn conventions

Quest data is registered via the Yarn file `quests/quests_index.yarn`. The registry is a
single Yarn node named `quests_index` that lists quest metadata, steps, and rewards.

### Manifest (`quests/index.json`)

The manifest is a JSON array of file names or relative paths. Entries without a `/` are
resolved relative to the `quests/` directory.

## Quest discovery

The quest registry is loaded from `quests/index.json` at runtime. JSON manifests
and directory listing are no longer used.

## Registry format

Inside the `quests_index` node body:
- Each quest starts with a `questId:` line.
- Optional fields: `displayName:`, `journalText:`, and `startNode:`.
- Steps are expressed as `step:` lines using `talk`, `collect`, or `kill`.
- Rewards are optional and use `reward.money:` and `reward.item:`.

## Rewards

Rewards are awarded when the quest is completed (via `QuestRewardEvent`). Rewards belong
to quests, not dialog effects.

Supported fields:

- `reward.money`: currency amount added to the player's wallet.
- `reward.item`: item definition IDs (for example, `sword`). Repeat the line per item.

### Example (single quest)

```yarn
title: quests_index
---
questId: welcome_to_town
displayName: Welcome to Town
journalText: Get to know the locals and settle in.
startNode: quest_notStarted
step: talk Npc-2
reward.money: 50
reward.item: sword
===
```
