# Quest JSON conventions

Each quest lives in its own JSON file. The filename should match the `questId`, and the
`questId` inside the JSON is the single source of truth used to register and look up the
quest definition.

### Manifest (`quests/index.json`)

The manifest is a JSON array of file names or relative paths. Entries without a `/` are
resolved relative to the `quests/` directory.

```json
[
  "Welcome_To_Town.json",
  "sidequests/Find_The_Sword.json"
]
```

### Directory listing

When using directory listing, every `.json` file in `quests/` is loaded. Keep filenames
aligned with the `questId` value so the asset layout remains easy to reason about.

## Quest discovery

Quests are loaded either by manifest (`quests/index.json`) or by listing the `quests/`
directory at runtime.

## Rewards

Quests can optionally define a `rewards` object. Rewards are awarded when the quest is
completed (via `QuestRewardEvent`). Rewards belong to quests, not dialog effects.

Supported fields:

- `money` (integer): currency amount added to the player's wallet.
- `items` (array of strings): item definition IDs (for example, `sword`).

### Example (single quest)

```json
{
  "questId": "Welcome_To_Town",
  "title": "Welcome to Town",
  "description": "Get to know the locals and settle in.",
  "steps": [
    {
      "type": "talk",
      "npc": "Npc-2"
    }
  ],
  "rewards": {
    "money": 50,
    "items": [
      "sword"
    ]
  }
}
```
