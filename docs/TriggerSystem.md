# TriggerSystem

## Purpose
Executes trigger logic for map sensors and delegates to specific TriggerHandlers.

## Flow
- Keeps a handler map by `Trigger.Type` and processes trigger queues each tick.
- Fires exit handlers on `ExitTriggerEvent`.
- Indexes entities with Tiled IDs to map triggers to real entities.

## Key components & events
- Trigger
- TriggerHandler
- ExitTriggerEvent
- Tiled
