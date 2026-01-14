# Dialog JSON conventions

## Effects

Dialog effects are intentionally limited to quest and flag state changes:

- `ADD_QUEST`
- `SET_FLAG`
- `QUEST_STEP`

Rewards (currency, items, experience, etc.) **must not** be applied through dialog effects. If dialog-triggered rewards become necessary in the future, add a **dedicated dialog event type** that is handled by a reward-specific system instead of mutating economy state directly from dialog processing.

## Nodes and choice branching

Dialogs can define named nodes and move to them from choices using a `next` reference. This enables branching without embedding all response lines directly in the choice.

### Defining nodes

Add a `nodes` array to the NPC dialog entry. Each node needs a unique `id` and can define its own `lines` and optional `choices`.

### Linking choices to nodes

Within `choices`, set `next` to the target node ID. When a choice is selected, the dialog system will jump to that node. If `next` is omitted or invalid, the system falls back to the legacy behavior and uses the choice `lines` instead.

### Example

```json
{
  "npcs": {
    "Shopkeeper": {
      "idle": [
        "Welcome in my shop!",
        "Would you like to see my wares?"
      ],
      "choices": [
        {
          "text": "Show me your wares.",
          "next": "shop_wares",
          "lines": [
            "I'm still setting up. Come back soon!"
          ]
        }
      ],
      "nodes": [
        {
          "id": "shop_wares",
          "lines": [
            "I'm still setting up. Come back soon!"
          ]
        }
      ]
    }
  }
}
```



## Example

```json
{
  "effects": [
    {
      "type": "SET_FLAG",
      "flag": "shop_seen",
      "value": true
    }
  ]
}
```
