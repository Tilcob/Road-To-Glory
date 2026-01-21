# StatModifierDurationSystem

## Zweck
Löscht abgelaufene Buff-Stat-Modifier und stößt Stat-Rekalkulationen an.

## Ablauf
- Iteriert Modifier, initialisiert Ablaufzeiten für buff:-Modifier und entfernt abgelaufene Einträge.
- Feuert StatRecalcEvent, wenn Modifier entfernt wurden.

## Wichtige Komponenten & Ereignisse
- StatModifierComponent
- StatRecalcEvent
- StatModifier
