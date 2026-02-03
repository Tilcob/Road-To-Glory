# MoveIntentSystem

## Purpose
Drives movement for non-NPC entities by translating `MoveIntent` targets into `Move` directions.

## Flow
- Reads active `MoveIntent` for entities with `Move` and `Transform` (excluding NPCs).
- Computes a direction toward the target position (or uses the intent direction).
- Clears the intent and stops movement when the entity reaches the arrival distance.

## Key components & events
- MoveIntent
- Move
- Transform
