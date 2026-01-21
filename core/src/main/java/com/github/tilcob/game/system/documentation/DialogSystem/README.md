# DialogSystem

## Purpose
Orchestrates dialogs between player and NPCs including choices, progression, and Yarn commands.

## Flow
- Starts dialogs when `Dialog.State.REQUEST`, creates `DialogSession` and `DialogNavigator`.
- Fires `DialogStartedEvent` and dispatches dialog line events until a choice or the end.
- Processes choice navigation/selection and emits `DialogChoiceResolvedEvent`.
- Ends dialogs on exit triggers or when no lines remain.

## Key components & events
- Dialog
- DialogNavigator
- DialogSession
- DialogYarnRuntime
- GameEventBus
