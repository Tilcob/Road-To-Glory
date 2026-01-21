# LifeSystem

## Zweck
Regeneriert Lebenspunkte und synchronisiert das UI für Player-Life.

## Ablauf
- Registriert einen EntityListener für Player-Life-Entities, um initiale UI-Updates zu senden.
- Regeneriert Life pro Sekunde bis MaxLife und aktualisiert das UI für Spieler-Entities.

## Wichtige Komponenten & Ereignisse
- Life
- Player
- GameViewModel
