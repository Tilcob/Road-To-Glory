# FacingSystem

## Zweck
Setzt die Blickrichtung basierend auf der Bewegungsrichtung, außer während der Attack-Windup-Phase.

## Ablauf
- Liest Move.direction und entscheidet zwischen horizontaler und vertikaler Dominanz.
- Überspringt Updates, wenn die Direction 0 ist oder ein Angriff im Windup ist.

## Wichtige Komponenten & Ereignisse
- Move
- Facing
- Attack
