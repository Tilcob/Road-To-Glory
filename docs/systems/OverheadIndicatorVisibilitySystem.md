# OverheadIndicatorVisibilitySystem

## Purpose
Applies distance-based visibility for overhead indicators with hysteresis to avoid edge flicker.

## Flow
- Iterates entities with `OverheadIndicator + Transform`.
- Computes distance to player (`Player + Transform`) every frame.
- Uses two thresholds:
    - `INDICATOR_SHOW_DISTANCE`
    - `INDICATOR_HIDE_DISTANCE` (must be larger)
- Hysteresis logic:
    - currently hidden → show when `distance <= show`
    - currently visible → keep visible until `distance >= hide`

## Why hysteresis
Without two thresholds, entities near the boundary rapidly toggle visible/hidden due to tiny movement or float jitter.

## Key components & services
- `OverheadIndicator`
- `Transform`
- `Player`
- `Constants`
