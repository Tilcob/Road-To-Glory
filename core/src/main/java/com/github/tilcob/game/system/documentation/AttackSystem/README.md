# AttackSystem

## Purpose
Executes melee attacks, plays SFX, roots the attacker during windup, and distributes damage to hit entities.

## Flow
- On attack start, plays SFX, sets `Move.rooted`, and clears the hit list.
- During the trigger phase, finds the matching attack sensor shape and runs an AABB query in the Box2D world.
- Filters hits (no self-hit, no FRIEND NPCs, only entities with `Life`).
- Creates/updates `Damaged` components with attack strength.

## Key components & events
- Attack
- Facing
- Physic
- Move
- Damaged
- Life
- World
