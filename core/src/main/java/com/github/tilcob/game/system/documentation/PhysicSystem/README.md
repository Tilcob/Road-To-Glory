# PhysicSystem

## Zweck
Steuert die Box2D-Simulation, interpoliert Positionen und verwaltet Trigger-Kontakte.

## Ablauf
- Stept die Box2D-World mit festem Timestep und speichert vorherige Positionen.
- Interpoliert Transform-Positionen zwischen Physik-Ticks für glattes Rendering.
- Verarbeitet Contact-Events, erstellt Trigger-Komponenten aus Sensoren und feuert ExitTriggerEvent.

## Wichtige Komponenten & Ereignisse
- World
- Physic
- Transform
- Trigger
- ExitTriggerEvent
