# DialogConsequenceSystem

## Zweck
Wendet Effekte aus aufgelösten Dialogentscheidungen an (Quests, Flags, Quest-Schritte).

## Ablauf
- Abonniert DialogChoiceResolvedEvent und iteriert über DialogEffect-Liste.
- Startet Quests, setzt Dialog-Flags oder signalisiert Quest-Schritte an den QuestManager.

## Wichtige Komponenten & Ereignisse
- DialogChoiceResolvedEvent
- DialogEffect
- QuestLifecycleService
- QuestManager
