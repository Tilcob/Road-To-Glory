# DialogSystem

## Zweck
Orchestriert Dialoge zwischen Spieler und NPCs inklusive Auswahl, Fortschritt und Yarn-Kommandos.

## Ablauf
- Startet Dialoge bei Dialog.State.REQUEST, erstellt DialogSession und DialogNavigator.
- Feuert DialogStartedEvent und dispatcht DialogLine-Events bis zur Auswahl oder zum Ende.
- Verarbeitet Choice-Navigation/Selection und löst DialogChoiceResolvedEvent aus.
- Beendet Dialoge bei ExitTrigger oder wenn keine Lines übrig sind.

## Wichtige Komponenten & Ereignisse
- Dialog
- DialogNavigator
- DialogSession
- DialogYarnRuntime
- GameEventBus
