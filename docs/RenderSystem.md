# RenderSystem

## Purpose
Renders tiled map layers and entities with `Transform`/`Graphic` in sorted order.

## Flow
- Renders background layers, then entities sorted by transform, then foreground layers.
- Draws sprites with scaling/rotation and sets batch color from `Graphic`.
- `setMap` orders layers in front of/behind the object layer.

## Key components & events
- OrthogonalTiledMapRenderer
- Transform
- Graphic
- Viewport
