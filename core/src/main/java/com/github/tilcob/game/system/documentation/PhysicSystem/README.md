# PhysicSystem

## Purpose
Drives the Box2D simulation, interpolates positions, and manages trigger contacts.

## Flow
- Steps the Box2D world with a fixed timestep and stores previous positions.
- Interpolates `Transform` positions between physics ticks for smooth rendering.
- Processes contact events, creates trigger components from sensors, and fires `ExitTriggerEvent`.

## Key components & events
- World
- Physic
- Transform
- Trigger
- ExitTriggerEvent
