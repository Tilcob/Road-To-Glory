# Item JSON conventions

Each item definition lives in its own JSON file under `assets/items/`. The `id` field is
stable and savegame-safe; it is the canonical identifier used in quests, saves, loot,
and inventory. File names should usually match the `id` to keep the asset layout easy to
reason about.

## Directory layout

Items are loaded by the manifest at `items/index.json`, similar to dialogs and quests.
Keep the manifest updated with every item file you want to ship.

Keep an `undefined.json` entry in this directory because the runtime resolves missing/null
item references to the `undefined` id.

## Required fields

- `id` (string): stable item id (example: `iron_sword`).
- `name` (string): display name.
- `category` (string): must match an `ItemCategory` enum value (example: `WEAPON`).
- `maxStack` (integer): stack size; use `1` for non-stackable items.
- `icon` (string): icon/drawable name (Skin/Atlas key).

## Optional fields

- `stats` (object): map of stat name → float value (example: `{"attack": 6}`). Stat names must exist in
  `assets/stats/index.json` to keep stat keys centralized.

## Example: stackable material

```json
{
  "id": "wood",
  "name": "Wood",
  "category": "MATERIAL",
  "maxStack": 99,
  "icon": "wood",
  "stats": {
    "burn_time": 1.5
  }
}
```

## Example: non-stackable weapon

```json
{
  "id": "iron_sword",
  "name": "Iron Sword",
  "category": "WEAPON",
  "maxStack": 1,
  "icon": "sword",
  "stats": {
    "attack": 6
  }
}
```
