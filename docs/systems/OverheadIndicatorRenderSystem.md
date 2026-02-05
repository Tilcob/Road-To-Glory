# OverheadIndicatorRenderSystem

## Purpose
Draws overhead indicator icons above entities in the world overlay pass (no UI stage).

## Flow
- Uses the world `Viewport` and `OrthographicCamera` for projection.
- Looks up the icon `TextureRegion` via `OverheadIndicatorRegistry`.
- Computes draw position from `Transform`, configured indicator offset, and animation offset.
- Applies animated scale, optional tint, and draws with `Batch`.

## Missing-atlas fallback behavior
- If a registered indicator region cannot be resolved from the atlas, the registry logs an error and returns `null`.
- The render system skips drawing that indicator frame instead of throwing.

This keeps the game running when placeholder keys (for example a temporary `INTERACT_HINT` icon) are configured before final art is packed.

## Key components & services
- `OverheadIndicator`
- `OverheadIndicatorAnimation`
- `Transform`
- `OverheadIndicatorRegistry`
- `AssetManager`
