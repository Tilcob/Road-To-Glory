# Skill Tree XP Distribution

This folder contains the XP distribution configuration used to split incoming XP across skill trees.

## File: `xp-distribution.json`

The file is a JSON object keyed by **source**. Each source maps to a JSON object of **tree IDs** with **weights**.

```json
{
  "combat": {
    "combat": 0.7,
    "vitality": 0.2,
    "agility": 0.1
  },
  "quest": {
    "combat": 0.7,
    "vitality": 0.2,
    "agility": 0.1
  }
}
```

### How it works
1. A system (for example `DamageSystem`) fires an `ExpGainRequestEvent` with a `source` and `baseXp`.
2. `ExpDistributionSystem` looks up the source entry in `xp-distribution.json`.
3. The weights are normalized and the total XP is split across the listed trees.
4. Each split fires an `ExpGainEvent` for the specific tree.

### Rules
- **Source keys** must be non-empty strings (e.g. `"combat"`, `"quest"`).
- **Tree IDs** must match a loaded skill tree id from `skill-trees/index.json`.
- **Weights** must be positive numbers (`> 0`). They are treated as proportions.
- Missing or empty distributions are ignored and will log errors in debug mode.

### Adjusting behavior
- To **share the same distribution across multiple sources**, copy the same weight map under each source key.
- To **bias XP toward a tree**, increase its weight relative to the others.
- To **add a new tree**, ensure it exists in `skill-trees/*.json` and `skill-trees/index.json`, then add it to the source map.
