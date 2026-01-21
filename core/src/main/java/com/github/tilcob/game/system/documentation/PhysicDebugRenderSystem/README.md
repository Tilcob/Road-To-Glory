# PhysicDebugRenderSystem

## Zweck
Rendert Box2D-Debug-Geometrie über die aktuelle Kameraansicht.

## Ablauf
- Ruft debugRenderer.render(world, camera.combined) im update auf.
- Gibt den Debug-Renderer in dispose frei.

## Wichtige Komponenten & Ereignisse
- World
- Box2DDebugRenderer
- Camera
