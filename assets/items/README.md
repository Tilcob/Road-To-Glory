# Item JSON conventions

Each item definition lives in its own JSON file under `assets/items/`. The `id` field is
stable and savegame-safe; it is the canonical identifier used in quests, saves, loot,
and inventory. File names should usually match the `id` to keep the asset layout easy to
reason about.

## Directory layout

Items are loaded by listing the `items/` directory at runtime. The loader is
**non-recursive** and ignores `index.json`, so place item files directly in this folder.

Keep an `undefined.json` entry in this directory because the runtime resolves missing/null
item references to the `undefined` id.

## Required fields

- `id` (string): stable item id (example: `iron_sword`).
- `name` (string): display name.
- `category` (string): must match an `ItemCategory` enum value (example: `WEAPON`).
- `maxStack` (integer): stack size; use `1` for non-stackable items.
- `icon` (string): icon/drawable name (Skin/Atlas key).

## Optional fields

- `stats` (object): map of stat name → float value (example: `{"attack": 6}`).

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
