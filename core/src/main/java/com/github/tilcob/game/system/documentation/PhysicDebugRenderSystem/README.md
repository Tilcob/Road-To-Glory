# PhysicDebugRenderSystem

## Purpose
Renders Box2D debug geometry over the current camera view.

## Flow
- Calls `debugRenderer.render(world, camera.combined)` in `update`.
- Disposes the debug renderer in `dispose`.

## Key components & events
- World
- Box2DDebugRenderer
- Camera
