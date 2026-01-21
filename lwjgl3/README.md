# LWJGL3 Launcher

This module provides the desktop launcher for **Road-To-Glory**. It wires in the core module, configures the LWJGL3 backend, and produces runnable JARs plus platform-specific builds.

## Responsibilities

- Launch the application via `com.github.tilcob.game.lwjgl3.Lwjgl3Launcher`.
- Include runtime assets from `assets/`.
- Package a cross-platform JAR or OS-specific JARs.
- Optional native-image/GraalVM support and Construo bundles.

## Important Gradle tasks

```bash
# Start the game
./gradlew lwjgl3:run

# Runnable JAR
./gradlew lwjgl3:jar

# OS-specific JARs
./gradlew lwjgl3:jarWin
./gradlew lwjgl3:jarMac
./gradlew lwjgl3:jarLinux
```

## Notes

- The `run` task uses `assets/` as the working directory.
- On macOS, `-XstartOnFirstThread` is set when needed.
