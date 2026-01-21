# LWJGL3-Launcher

Dieses Modul stellt den Desktop-Launcher für **Road-To-Glory** bereit. Es bindet das Core-Modul ein, richtet das LWJGL3-Backend ein und erzeugt ausführbare JARs sowie plattformspezifische Builds.

## Aufgaben

- Start der Anwendung über die Klasse `com.github.tilcob.game.lwjgl3.Lwjgl3Launcher`.
- Einbindung der Runtime-Assets (`assets/`).
- Packaging als Cross-Platform-JAR oder OS-spezifische JARs.
- Optionaler Native-Image/GraalVM-Support und Construo-Bundles.

## Wichtige Gradle-Tasks

```bash
# Spiel starten
./gradlew lwjgl3:run

# Ausführbares JAR
./gradlew lwjgl3:jar

# OS-spezifische JARs
./gradlew lwjgl3:jarWin
./gradlew lwjgl3:jarMac
./gradlew lwjgl3:jarLinux
```

## Hinweise

- Das `run`-Task nutzt `assets/` als Working Directory.
- Für macOS wird bei Bedarf `-XstartOnFirstThread` gesetzt.
