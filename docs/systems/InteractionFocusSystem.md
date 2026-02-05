# InteractionFocusSystem

## Purpose
Selects the current **best interaction target** and stores it in `ActiveEntityReference`.

## Flow
- Queries player (`Player + Transform`) and candidates (`Interactable + Transform`).
- Scores each candidate by:
    - proximity (closer is better)
    - front-facing bonus (uses player `Facing` when available)
    - `Interactable.priority` bonus
- Keeps the highest-scoring candidate within interaction/focus distance.
- Writes the selected entity to `ActiveEntityReference.setFocused(...)`.
- Clears focus when no valid target exists.

## Notes
- This system does not start interactions directly.
- It provides shared focus context used by indicator state resolution and future input/UI hints.

## Key components & services
- `Player`
- `Transform`
- `Facing`
- `Interactable`
- `ActiveEntityReference`
