# SkillSystem

## Purpose
Manages character progression through modular skill trees. It handles experience gain, leveling up within specific trees, and unlocking skill nodes that apply stat modifiers.

## Flow
- **XP Gain**:Listens for `XPGainEvent`. Finds the target skill tree in the entity's `SkillComponent` and adds XP. Triggers level-ups if thresholds are met.
- **Level Up**: When a tree levels up, it updates the `SkillTreeState` (incrementing level and points) and fires a `LevelUpEvent` with the specific `treeId`.
- **Node Unlocking**: Listens for `SkillUnlockEvent`. Verifies if the entity has enough points, meets the level requirement, and has unlocked parent nodes. If successful, it deducts points, marks the node as unlocked, and applies the node's stat modifiers to the entity's `StatComponent`.

## Key components & events
- **Components**: `SkillComponent` (holds state), `SkillTreeState` (runtime data).
- **Events**: `XPGainEvent`, `LevelUpEvent`, `SkillUnlockEvent`.
- **Data**: `SkillTreeDefinition` (from JSON), `SkillNodeDefinition`.
