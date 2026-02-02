# SkillSystem

## Purpose
Manages character progression through modular skill trees. It handles experience gain, leveling up within specific trees, and unlocking skill nodes that apply stat modifiers.

## Flow
- **XP Gain**: Listens for `XPGainEvent`. Finds the target skill tree in the entity's `SkillComponent` and adds XP. Triggers level-ups if thresholds are met.
- **Level Up**: When a tree levels up, it updates the `SkillTreeState` (incrementing level and points) and fires a `LevelUpEvent` with the specific `treeId`.
- **Node Unlocking**: Listens for `SkillUnlockEvent`. Verifies if the entity has enough points, meets the level requirement, and has unlocked parent nodes. If successful, it deducts points, marks the node as unlocked, and applies the node's stat modifiers to the entity's `StatComponent`.

## Tuning XP and skill points
- **XP per kill (or other actions)**: XP rewards are granted by firing `XPGainEvent`. The enemy kill reward is currently set in `DamageSystem` when an `NpcType.ENEMY` dies. Adjust the `new XPGainEvent(player, "combat", 50)` amount to control XP per kill for the combat tree, or add additional `XPGainEvent` calls for other actions. This is the place to change how much XP a kill grants.
- **XP to level**: Each skill tree has an `xpTable` in its JSON definition (for example `assets/skill-trees/combat.json`). The table entries define the XP required to reach each level. Edit the array values to change how much XP is needed for leveling in that tree.
- **Skill points per level**: Skill points are incremented on level-up inside `SkillSystem.checkLevelUp` via `state.setSkillPoints(state.getSkillPoints() + 1)`. Change that `+ 1` to grant more (or fewer) points per level.

## Key components and events
- **Components**: `SkillComponent` (holds state), `SkillTreeState` (runtime data).
- **Events**: `XPGainEvent`, `LevelUpEvent`, `SkillUnlockEvent`.
- **Data**: `SkillTreeDefinition` (from JSON), `SkillNodeDefinition`.
