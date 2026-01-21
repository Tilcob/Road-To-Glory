# RenderSystem

## Zweck
Rendert Tiled-Map-Layer und Entities mit Transform/Graphic in sortierter Reihenfolge.

## Ablauf
- Rendert Hintergrund-Layer, dann Entities sortiert nach Transform, dann Vordergrund-Layer.
- Zeichnet Sprites mit Skalierung/Rotation und setzt Batch-Farbe aus Graphic.
- setMap ordnet die Layer vor/hinter dem Object-Layer.

## Wichtige Komponenten & Ereignisse
- OrthogonalTiledMapRenderer
- Transform
- Graphic
- Viewport
