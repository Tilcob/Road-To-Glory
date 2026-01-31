# Road-To-Glory

Road-To-Glory is a LibGDX-based RPG with a modular architecture split between shared core logic and the LWJGL3 desktop launcher.

## Repository layout

- `core`: Shared game logic, assets management, ECS components/systems, and UI.
- `lwjgl3`: Desktop launcher and platform-specific configuration.
- `assets`: Runtime assets bundled with the game.
- `assets_raw`: Source assets before processing/export.
- `utils`: Utility scripts and tooling (content validation, index generation).
- `skinComposer.scmp`: Skin composer project file used for UI skin assets.

## Content pipeline

The game loads structured content that is pre-indexed for fast lookup. Index files are generated from the source content
and kept in `assets` for runtime loading.

### Quest data

Quest definitions live in `assets/quests/index.json`, which is generated from the
`.yarn` quest files in `assets/quests` and loaded from Yarn headers inside each quest
file.

### Dialog data

Dialog definitions live in `assets/dialogs/index.json`, which is generated from the
`.yarn` dialog files in `assets/dialogs` and loaded from the file name.

### Cutscene data

Cutscene scripts live in `assets/cutscenes` as `.yarn` files and are loaded by cutscene
id. Cutscenes support the same Yarn command line syntax as dialogs, plus custom
commands such as `<<wait_for_camera>>` and `<<wait_for_move>>` to pause until camera
pans or scripted movement completes.

### Item data

Item definitions live in `assets/items/index.json`, which is generated from the
`.json` quest files in `assets/items` and loaded from file name.

## Codebase documentation

The core module follows a layered architecture on top of Ashley ECS. The most important packages are documented via
`package-info.java` so that IDEs show clear package-level overviews. The highlights are:

- `com.github.tilcob.game.system`: ECS systems for gameplay, AI, rendering, dialog, and inventory.
- `com.github.tilcob.game.component`: Data-only ECS components for entity state.
- `com.github.tilcob.game.ai`: NPC state/behavior configuration and AI state machines.
- `com.github.tilcob.game.dialog`: Dialog data, selection logic, and Yarn loader integration.
- `com.github.tilcob.game.quest`: Quest definitions, steps, and rewards.
- `com.github.tilcob.game.save`: Save service, registry, and migration helpers.
- `com.github.tilcob.game.ui`: Scene2D UI views, models, and inventory widgets.

When introducing new packages or major gameplay features, follow the same pattern by adding or extending
`package-info.java` descriptions so the high-level intent stays discoverable.

## Debug / Strict Mode (Yarn)

If the debug mode is enabled with key `F5` you can hot reload the assets except the ui, maps, and the visuals.
The Yarn Expression Engine supports a **Strict Mode** that is
automatically coupled to the debug state:

- **Debug enabled** → `BaseYarnRuntime.strict == true`
- **Debug disabled** → `BaseYarnRuntime.strict == false`

The debug state is determined via: `runtime.isDebugMode()`

### What Strict Mode Means

- **Strict mode**
    - Operand types must match exactly
    - Example: `"1" == 1` → **false**

- **Non-strict mode**
    - Limited, safe fallback comparisons may apply
    - Example: string fallback via `String.valueOf(...)`
    - Only used when types are not dangerous

Important rule (always enforced):
- **Boolean ↔ Number comparisons are NEVER allowed**
    - `true == 1` → **false** (always)

### Expression Debug Logging

When debug logging is enabled (`LogLevel.DEBUG`), the expression engine
emits additional diagnostics under the log tag:

## Quick start

### Run the game

```bash
./gradlew lwjgl3:run
```

To enable debug diagnostics (profiler, FPS logging, debug renderer), pass the JVM flag:

```bash
./gradlew lwjgl3:run -Dgame.debug=true
```

### Run tests

```bash
./gradlew test
```

To run the full verification pipeline (tests plus content validation), use:

```bash
./gradlew check
```

### Validate quest + dialog content

```bash
./gradlew :utils:validateQuestContent
```

This validation checks quest headers, step definitions, start node presence, and dialog quest tags against the quest index. It also runs automatically as part of `./gradlew check` and before `lwjgl3:run` or `lwjgl3:build`.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.

### Build configuration flags

These are defined in `gradle.properties` and can be overridden on the command line.

- `useMavenLocal`: enable `mavenLocal()` repositories for local dependency testing.
- `useSnapshots`: enable snapshot repository access.
- `enableDependencyLocking`: enable dependency locking (run `./gradlew --write-locks` to generate lock files).
- `generateAssetList`: controls whether `assets/assets.txt` is generated during `processResources`.

## Release checklist (local)

- Run `./gradlew clean build` to confirm builds and tests pass.
- Run `./gradlew :utils:validateQuestContent` to validate quest/dialog content.
- Use `./gradlew lwjgl3:jar` to create the desktop-runnable jar in `lwjgl3/build/libs`.
