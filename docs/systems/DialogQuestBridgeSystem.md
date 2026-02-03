# DialogQuestBridgeSystem

## Purpose
Connects dialog endings with quest and NPC logic (first-contact flags, quest talk, FSM message).

## Flow
- On `DialogFinishedEvent`: sets the first-contact flag in the player's `DialogFlags` container.
- Signals "talk" to the `QuestManager`.
- Notifies the NPC FSM via `Messages.DIALOG_FINISHED`.

## Key components & events
- DialogFinishedEvent
- DialogFlags
- QuestManager
- NpcFsm
