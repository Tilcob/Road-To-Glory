# ControllerSystem

## Zweck
Übersetzt Eingabekommandos in Bewegungs- und Interaktionsaktionen für Entities mit Controller.

## Ablauf
- Setzt die Bewegungsrichtung basierend auf gehaltenen Commands (blockiert bei Dialogauswahl).
- Verarbeitet Commands aus dem Command-Buffer (z. B. Attack, Pause, Interact, Inventory).
- Steuert Dialog-Navigation, startet Angriffe und löst Interaktionen mit Truhen oder NPCs aus.

## Wichtige Komponenten & Ereignisse
- Controller
- Move
- DialogSession
- GameEventBus
- Command
