# DialogConsequenceSystem

## Purpose
Applies effects from resolved dialog decisions (quests, flags, quest steps).

## Flow
- Subscribes to `DialogChoiceResolvedEvent` and iterates over the `DialogEffect` list.
- Starts quests, sets dialog flags, or signals quest steps to the `QuestManager`.

## Key components & events
- DialogChoiceResolvedEvent
- DialogEffect
- QuestLifecycleService
- QuestManager
