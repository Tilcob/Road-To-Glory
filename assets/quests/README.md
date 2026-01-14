# Quest JSON conventions

## Rewards

Quests can optionally define a `rewards` object. Rewards are awarded when the quest is
completed (via `QuestCompletedEvent`). Rewards belong to quests, not dialog effects.

Supported fields:

- `gold` (integer): currency amount added to the player's wallet.
- `items` (array of strings): item type names matching `ItemType` entries.

### Example

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
    "gold": 50,
    "items": [
      "SWORD"
    ]
  }
}
```
