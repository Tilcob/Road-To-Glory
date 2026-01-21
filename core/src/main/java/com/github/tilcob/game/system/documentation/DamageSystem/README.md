# DamageSystem

## Purpose
Processes damage events, reduces life, removes dead NPCs, and triggers quest signals.

## Flow
- Removes the `Damaged` component and reduces life accordingly.
- When an enemy NPC dies, sends a "kill" signal to the `QuestManager`.
- Generates damage feedback via `GameViewModel` (e.g., damage numbers).

## Key components & events
- Damaged
- Life
- Npc
- QuestManager
- GameViewModel
