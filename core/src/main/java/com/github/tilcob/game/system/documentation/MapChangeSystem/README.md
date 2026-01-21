# MapChangeSystem

## Zweck
Wechselt Karten, speichert den Zustand und repositioniert den Spieler an den Spawnpunkt.

## Ablauf
- Speichert Map-Zustand, lädt die neue Map und liest den Spawnpunkt aus dem TiledManager.
- Feuert AutosaveEvent und MapChangeEvent.
- Setzt Transform/Physic-Position auf den Spawn und entfernt MapChange.

## Wichtige Komponenten & Ereignisse
- MapChange
- TiledManager
- StateManager
- MapChangeEvent
