# SkillSystem

## Purpose
Manages character progression through modular skill trees. 
It handles experience gain, leveling up within specific trees, 
and unlocking skill nodes that apply stat modifiers. 
Level-up points are earned into a shared pool that can be spent across all trees.

## Flow
- **XP Gain**: Listens for `ExpGainEvent`. Finds the target skill tree in the entity's `Skill` component and 
- adds XP. Triggers level-ups if thresholds are met.
- **Level Up**: When a tree levels up, it updates the `SkillTreeState` (incrementing level) and 
- adds points to the shared pool in `Skill`, then fires a `LevelUpEvent` with the specific `treeId`.
- **Node Unlocking**: Listens for `SkillUnlockEvent`. Verifies if the entity has enough shared points, 
meets the level requirement, and has unlocked parent nodes. If successful, it deducts shared points, 
marks the node as unlocked, and applies the node's stat modifiers to the entity's `StatModifierComponent`.

## Tuning XP and skill points
- **XP per kill (or other actions)**: XP rewards are granted by firing `ExpGainRequestEvent`, which is then distributed to trees by `ExpDistributionSystem`. The enemy kill reward is currently set in `DamageSystem` when an `NpcType.ENEMY` dies. Adjust the base XP passed to `new ExpGainRequestEvent(...)` to control XP per kill for the combat source, or add additional requests for other actions. This is the place to change how much XP a kill grants.
- **XP distribution per source**: The per-source weightings live in `assets/skill-trees/xp-distribution.json` (for example, `combat` and `quest`). Edit these weights to change how XP is split across trees for each source.
- **XP to level**: Each skill tree has an `xpTable` in its JSON definition (for example `assets/skill-trees/combat.json`). The table entries define the XP required to reach each level. Edit the array values to change how much XP is needed for leveling in that tree.
- **Skill points per level**: Skill points are incremented on level-up inside `SkillSystem.checkLevelUp` via `Skill.addSharedSkillPoints(1)`. Change that `+ 1` to grant more (or fewer) points per level.

## Key components and events
- **Components**: `Skill` (holds state + shared points), `SkillTreeState` (runtime data).
- **Events**: `ExpGainEvent`, `ExpGainRequestEvent`, `LevelUpEvent`, `SkillUnlockEvent`.
- **Data**: `SkillTreeDefinition` (from JSON), `SkillNodeDefinition`.
