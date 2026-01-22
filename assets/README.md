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
