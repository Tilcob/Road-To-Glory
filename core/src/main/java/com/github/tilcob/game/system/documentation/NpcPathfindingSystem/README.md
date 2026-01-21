# NpcPathfindingSystem

## Zweck
Übersetzt MoveIntent in eine normalisierte Bewegungsrichtung für NPCs.

## Ablauf
- Wenn Intent inaktiv ist, wird die Richtung auf 0 gesetzt.
- Bei Ziel-Intent wird der Vektor zum Ziel berechnet und bei Ankunft der Intent gelöscht.
- Die resultierende Richtung wird normalisiert und in Move.direction geschrieben.

## Wichtige Komponenten & Ereignisse
- MoveIntent
- Move
- Npc
- Transform
