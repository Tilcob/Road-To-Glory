# OverheadIndicatorStateSystem

## Purpose
Resolves the correct overhead indicator type based on NPC role and quest/dialog progress.

## Flow
- Iterates over entities with `OverheadIndicator`, `NpcRole`, and `Npc`.
- Checks quest state (available, active, ready to turn in) using dialog metadata and the quest registry.
- Falls back to role-based indicators when no quest-specific state is active.
- Writes the resolved `OverheadIndicatorType` into the `OverheadIndicator` component.

## Key components & services
- OverheadIndicator
- NpcRole
- Npc
- QuestLog
- QuestYarnRegistry
