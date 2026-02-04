# OverheadIndicatorRenderSystem

## Purpose
Draws overhead indicator icons above NPCs and other map objects in the world render pass (no UI stage).

## Flow
- Uses the world `Viewport` and `OrthographicCamera` for projection.
- Looks up the icon `TextureRegion` via `OverheadIndicatorRegistry`.
- Computes the draw position from `Transform`, base offset, and the animated bob offset.
- Renders with the animated scale and optional tint color.

## Key components & services
- OverheadIndicator
- OverheadIndicatorAnimation
- Transform
- OverheadIndicatorRegistry
- AssetManager
