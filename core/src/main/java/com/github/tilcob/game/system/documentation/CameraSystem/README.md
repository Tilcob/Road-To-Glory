# CameraSystem

## Purpose
Moves the camera smoothly toward the target position and clamps it to map boundaries.

## Flow
- Computes a target position from `Transform` + camera offset.
- Clamps the target position against the map size and interpolates with a smoothing factor.
- `setMap` reads tile and map size from Tiled properties and initializes camera position.

## Key components & events
- CameraFollow
- Transform
- TiledMap
- Camera
