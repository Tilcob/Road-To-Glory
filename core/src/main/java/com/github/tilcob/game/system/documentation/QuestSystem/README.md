# QuestSystem

## Zweck
Überwacht aktive Quests und startet neue Quests über Events.

## Ablauf
- Iteriert über QuestLog-Quests und ruft notifyQuestCompletion auf.
- Abonniert AddQuestEvent, um QuestLifecycleService.startQuest aufzurufen.

## Wichtige Komponenten & Ereignisse
- QuestLog
- QuestLifecycleService
- AddQuestEvent
