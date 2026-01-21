# QuestSystem

## Purpose
Monitors active quests and starts new quests through events.

## Flow
- Iterates over `QuestLog` quests and calls `notifyQuestCompletion`.
- Subscribes to `AddQuestEvent` to call `QuestLifecycleService.startQuest`.

## Key components & events
- QuestLog
- QuestLifecycleService
- AddQuestEvent
