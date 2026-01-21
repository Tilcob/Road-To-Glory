# LevelUpSystem

## Purpose
Grants level-up stat modifiers and triggers a stat recalculation.

## Flow
- Receives `LevelUpEvent` and ensures `StatComponent`/`StatModifierComponent` exist.
- Removes old `levelup:` modifiers and adds new modifiers based on `totalLevels`.
- Fires `StatRecalcEvent` and writes debug logs.

## Key components & events
- LevelUpEvent
- StatModifierComponent
- StatRecalcEvent
