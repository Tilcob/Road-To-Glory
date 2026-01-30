# Assets

This directory contains the runtime assets for **Road-To-Glory**. The game loads these files directly as resources in the desktop app.

## Structure (excerpt)

- `audio/`: sound effects and music.
- `graphics/`: textures, spritesheets, and graphics.
- `maps/`: maps and tilemap data.
- `dialogs/`: dialog files (Yarn).
- `cutscenes/`: cutscene scripts (Yarn).
- `quests/`: quest definitions and `index.json`.
- `items/`: item definitions.
- `ui/`: UI skins, layouts, and UI assets.
- `stats/`: stat and balancing data.
- `tests/`: test/sample data for content.

## Notes

- `quests/index.json`, `dialogs/index.json` and `items/index.json` are generated from the `.yarn` or `.json` files.
- The game starts with `assets/` as the working directory (for example, via `lwjgl3:run`).

### Debug / Strict Mode (Dialogs & Quests)

Dialog and quest conditions are evaluated using the internal
**Yarn Expression Engine**.

- **Debug enabled** → Strict mode enabled
- **Debug disabled** → Strict mode disabled

Strict mode controls how strict types are handled in expressions.

Regardless of strict mode:
- **Boolean ↔ Number conversion is NEVER allowed**
- Any comparison between `Boolean` and `Number` is always invalid
