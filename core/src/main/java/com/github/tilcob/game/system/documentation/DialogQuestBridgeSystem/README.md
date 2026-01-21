# DialogQuestBridgeSystem

## Zweck
Verbindet Dialogende mit Quest- und NPC-Logik (First-Contact-Flags, Quest-Talk, FSM-Nachricht).

## Ablauf
- Bei DialogFinishedEvent: setzt First-Contact-Flag im DialogFlags-Container des Spielers.
- Signaliert "talk" an den QuestManager.
- Benachrichtigt die NPC-FSM über Messages.DIALOG_FINISHED.

## Wichtige Komponenten & Ereignisse
- DialogFinishedEvent
- DialogFlags
- QuestManager
- NpcFsm
