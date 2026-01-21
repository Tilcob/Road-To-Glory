# FacingSystem

## Purpose
Sets facing direction based on movement direction, except during the attack windup phase.

## Flow
- Reads `Move.direction` and decides between horizontal and vertical dominance.
- Skips updates when direction is 0 or an attack is in windup.

## Key components & events
- Move
- Facing
- Attack
