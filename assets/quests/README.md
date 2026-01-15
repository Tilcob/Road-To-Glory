# Quest JSON conventions

Each quest lives in its own JSON file. The filename (and matching `QuestAsset` enum
name) must equal the `questId`.

## Rewards

Quests can optionally define a `rewards` object. Rewards are awarded when the quest is
completed (via `QuestRewardEvent`). Rewards belong to quests, not dialog effects.

Supported fields:

- `money` (integer): currency amount added to the player's wallet.
- `items` (array of strings): item type names matching `ItemType` entries.

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
      "SWORD"
    ]
  }
}
```
