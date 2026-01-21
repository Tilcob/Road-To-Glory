# PhysicMoveSystem

## Purpose
Transfers movement direction into Box2D linear velocity for physics entities.

## Flow
- Sets velocity to 0 when rooted or direction=0.
- Normalizes direction and scales by `Move.maxSpeed`.

## Key components & events
- Move
- Physic
- Body
