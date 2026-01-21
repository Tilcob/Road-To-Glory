# QuestRewardSchedulerSystem

## Purpose
Schedules quest rewards after dialogs or quest completions.

## Flow
- On `DialogFinishedEvent`: `scheduleRewardFromDialog`.
- On `QuestCompletedEvent`: `scheduleRewardFromCompletion`.

## Key components & events
- DialogFinishedEvent
- QuestCompletedEvent
- QuestLifecycleService
