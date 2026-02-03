# LevelUpSystem

## Purpose
Grants level-up stat modifiers and triggers a stat recalculation.

## Flow
- Receives `LevelUpEvent`.
- **Checks `treeId`**: Only processes events with `treeId="base"`. Skill tree level-ups are handled by `SkillSystem`.
- Removes old `levelup:` modifiers and adds new modifiers based on `totalLevels`.
- Fires `StatRecalcEvent` and writes debug logs.

## Key components & events
- LevelUpEvent
- StatModifierComponent
- StatRecalcEvent
