# Quest Yarn conventions

Quest data is registered via Yarn headers inside each quest file referenced by the
`quests/index.json` manifest (generated from `.yarn` files only).

### Manifest (`quests/index.json`)

The manifest is a JSON array of `.yarn` file names or relative paths. Entries without a
`/` are resolved relative to the `quests/` directory.

## Quest discovery

The quest registry is loaded from `quests/index.json` at runtime. JSON manifests
and directory listing are no longer used. Do not add metadata files like
`quests_index.yarn`; only `.yarn` quest scripts belong here.

## Registry format

Inside the quest file headers (before the first `---`):
- Each quest starts with a `questId:` line.
- Required fields: `displayName:`, `journalText:`, and `startNode:`.
- Steps are expressed as `step:` lines using `talk`, `collect`, or `kill`.
- Rewards are optional and use `reward_money:` / `reward_item:`.
- You may also use `reward_items:` with a comma-separated list.
- Reward timing is optional and uses `reward_timing:` (`giver`, `completion`, or `auto`).

## Rewards

Rewards are awarded when the quest is completed (via `QuestRewardEvent`). Rewards belong
to quests, not dialog effects.

Supported fields:

- `reward_money`: currency amount added to the player's wallet.
- `reward_item`: item definition IDs (for example, `sword`). Repeat the line per item.
- `reward_items`: comma-separated item definition IDs.
- `reward_timing`: when rewards are delivered. Use `giver`, `completion`, or `auto` (defaults to `giver`).

### Quest Yarn runtime commands (executed from quest nodes)

These commands are executed when quest nodes are processed (for example `q_<questId>_start` or
`q_<questId>_on_<event>`).

Quest nodes should only drive quest state and flags; rewards belong in the header fields above.

- `<<quest_start <questId>>>`: Mark a quest as started (adds it to the quest log).
- `<<quest_stage <questId> <stage>>>`: Set the quest stage (0-based).
- `<<quest_complete <questId>>>`: Mark the quest as completed and emit completion events.
- `<<set_flag <flag> <true|false>>>`: Set a dialog flag (boolean).
- `<<inc_counter <counter> <amount>>>`: Increment a named counter (amount defaults to `1`).


### Example (single quest)

```yarn
questId: welcome_to_town
displayName: Welcome to Town
journalText: Get to know the locals and settle in.
startNode: q_welcome_to_town_start
reward_money: 50
reward_item: sword
reward_timing: giver
step: talk Npc-2
title: q_welcome_to_town_start
position: -178,-155
---
<<quest_start welcome_to_town>>
<<quest_stage welcome_to_town 0>>
===

title: q_welcome_to_town_on_talk
position: -178,-155
---
<<if $eventTarget == "Npc-2">>
<<quest_stage welcome_to_town 1>>
<<quest_complete welcome_to_town>>
<<endif>>
===

```
