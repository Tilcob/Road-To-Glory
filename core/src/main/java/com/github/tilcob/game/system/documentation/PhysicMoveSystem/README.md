# PhysicMoveSystem

## Zweck
Überträgt Move-Richtung in Box2D-Linearvelocity für Physic-Entities.

## Ablauf
- Setzt die Geschwindigkeit auf 0, wenn rooted oder direction=0 ist.
- Normalisiert die Richtung und skaliert mit Move.maxSpeed.

## Wichtige Komponenten & Ereignisse
- Move
- Physic
- Body
