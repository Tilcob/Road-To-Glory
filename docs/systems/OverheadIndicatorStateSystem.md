# OverheadIndicatorStateSystem

## Purpose
Resolves **one deterministic overhead indicator type per NPC** from role, quest, and dialog context.

## Flow
- Iterates over entities with `OverheadIndicator`, `NpcRole`, and `Npc`.
- Evaluates role-based candidates (`DANGER`, `MERCHANT`, `INFO`, etc.).
- Evaluates dialog candidates using runtime context:
    - focused target from `ActiveEntityReference`
    - interaction range check vs player transform
    - dialog busy state (`Dialog.State.ACTIVE`)
    - dialog choice state (`DialogSession.isAwaitingChoice()`)
- Evaluates quest candidates from `QuestLog` + `QuestYarnRegistry` + dialog metadata.
- Selects the **highest-priority** candidate and writes it to `OverheadIndicator.indicatorId`.

## Priority model
Current resolution order is implemented as numeric priority:

1. `QUEST_TURNING`
2. `QUEST_AVAILABLE`
3. `DANGER` / `ANGRY`
4. `MERCHANT`
5. `TALK_*` (`TALK_BUSY`, `TALK_CHOICE`, `INTERACT_HINT`, `TALK_IN_RANGE`, `TALK_AVAILABLE`, `TALKING`)
6. fallback/low-priority states (e.g. `INFO`)

This guarantees quest indicators stay dominant over dialog hints.

## Dialog indicator mapping
- Not interactable → no talk-specific candidate.
- Interactable but not focused → `TALK_AVAILABLE`.
- Interactable + focused + in range → `INTERACT_HINT`.
- Interactable + focused + out of range → `TALK_IN_RANGE`.
- Active dialog on NPC → `TALK_BUSY`.
- Active dialog choice for player on NPC → `TALK_CHOICE`.

## Key components & services
- `OverheadIndicator`
- `NpcRole`
- `Npc`
- `Interactable`
- `Dialog`, `DialogSession`
- `ActiveEntityReference`
- `QuestLog`
- `QuestYarnRegistry`
