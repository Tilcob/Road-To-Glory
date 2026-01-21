# QuestRewardSchedulerSystem

## Zweck
Plant Quest-Belohnungen nach Dialogen oder Questabschlüssen.

## Ablauf
- Bei DialogFinishedEvent: scheduleRewardFromDialog.
- Bei QuestCompletedEvent: scheduleRewardFromCompletion.

## Wichtige Komponenten & Ereignisse
- DialogFinishedEvent
- QuestCompletedEvent
- QuestLifecycleService
