# NpcPathfindingSystem

## Purpose
Translates `MoveIntent` into a normalized movement direction for NPCs.

## Flow
- If the intent is inactive, sets direction to 0.
- For target intent, computes the vector to the target and clears the intent on arrival.
- Normalizes the resulting direction and writes it into `Move.direction`.

## Key components & events
- MoveIntent
- Move
- Npc
- Transform
