# Dialog JSON conventions

## Effects

Dialog effects are intentionally limited to quest and flag state changes:

- `ADD_QUEST`
- `SET_FLAG`
- `QUEST_STEP`

Rewards (currency, items, experience, etc.) **must not** be applied through dialog effects. If dialog-triggered rewards become necessary in the future, add a **dedicated dialog event type** that is handled by a reward-specific system instead of mutating economy state directly from dialog processing.

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
