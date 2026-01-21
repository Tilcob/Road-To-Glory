# AttackSystem

## Zweck
Führt Nahkampfangriffe aus, spielt SFX, verwurzelt den Angreifer während der Windup-Phase und verteilt Schaden an getroffenen Entities.

## Ablauf
- Startet beim Angriff die SFX, setzt Move.rooted und leert die Trefferliste.
- Während der Trigger-Phase wird die passende Angriffssensor-Form gesucht und ein AABB-Query in der Box2D-World ausgeführt.
- Treffer werden gefiltert (kein Selbsttreffer, keine FRIEND-NPCs, nur Entities mit Life).
- Erzeugt/aktualisiert Damaged-Komponenten mit der Angriffs-Stärke.

## Wichtige Komponenten & Ereignisse
- Attack
- Facing
- Physic
- Move
- Damaged
- Life
- World
