# LifeSystem

## Purpose
Regenerates health and syncs the UI for player life.

## Flow
- Registers an `EntityListener` for player life entities to send initial UI updates.
- Regenerates life per second up to `MaxLife` and updates the UI for player entities.

## Key components & events
- Life
- Player
- GameViewModel
