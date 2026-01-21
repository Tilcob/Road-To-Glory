# Road-To-Glory

A LibGDX-based RPG project with a modular architecture split between shared core logic and the LWJGL3 desktop launcher.

## Repository layout

- `core`: Shared game logic, assets management, ECS components/systems, and UI.
- `lwjgl3`: Desktop launcher and platform-specific configuration.
- `assets`: Runtime assets bundled with the game.
- `assets_raw`: Source assets before processing/export.
- `utils`: Utility scripts and tooling.

### Quest data

Quest definitions live in `assets/quests/index.json`, which is generated from the
`.yarn` quest files in `assets/quests` and loaded from Yarn headers inside each quest
file.

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

## Development

### Run the game

```
./gradlew lwjgl3:run
```

To enable debug diagnostics (profiler, FPS logging, debug renderer), pass the JVM flag:

```
./gradlew lwjgl3:run -Dgame.debug=true
```

### Run tests

```
./gradlew test
```

### Validate quest + dialog content

```
./gradlew :utils:validateQuestContent
```

This validation checks quest headers, step definitions, start node presence, and dialog quest tags against the quest index.

### Build configuration flags

These are defined in `gradle.properties` and can be overridden on the command line.

- `useMavenLocal`: enable `mavenLocal()` repositories for local dependency testing.
- `useSnapshots`: enable snapshot repository access.
- `enableDependencyLocking`: enable dependency locking (run `./gradlew --write-locks` to generate lock files).
- `generateAssetList`: controls whether `assets/assets.txt` is generated during `processResources`.
