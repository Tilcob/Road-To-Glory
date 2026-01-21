# MapChangeSystem

## Purpose
Changes maps, saves state, and repositions the player at the spawn point.

## Flow
- Saves map state, loads the new map, and reads the spawn point from the `TiledManager`.
- Fires `AutosaveEvent` and `MapChangeEvent`.
- Sets Transform/Physic position to the spawn and removes `MapChange`.

## Key components & events
- MapChange
- TiledManager
- StateManager
- MapChangeEvent
