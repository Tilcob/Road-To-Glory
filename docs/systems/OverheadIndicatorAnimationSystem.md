# OverheadIndicatorAnimationSystem

## Purpose
Applies lightweight bob and pulse animations to overhead indicators by updating the runtime animation state each frame.

## Flow
- Iterates over entities with `OverheadIndicator`, `OverheadIndicatorAnimation`, and `Transform`.
- Advances the animation timer and computes bob offset and pulse scale.
- Stores the computed `currentOffsetY` and `currentScale` in `OverheadIndicatorAnimation` for the render pass.
- Respects per-entity flags for enabling/disabling bob and pulse.

## Key components & services
- OverheadIndicator
- OverheadIndicatorAnimation
- Transform
