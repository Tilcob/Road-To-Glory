# AiSystem

## Purpose
Drives NPC AI and ensures each NPC entity has a reference to the player before the state machine updates.

## Flow
- Filters entities with `Npc` and without `Player`.
- Sets `PlayerReference` to the first found player entity when needed.
- Updates the `NpcFsm` state machine each tick.

## Key components & events
- Npc
- Player
- PlayerReference
- NpcFsm
