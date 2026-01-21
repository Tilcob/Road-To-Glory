# Utils

Dieses Modul enthält Hilfstools und Validierungslogik für das Projekt. Aktuell liegt der Fokus auf der Prüfung von Quest- und Dialogdaten.

## Inhalte

- **QuestContentValidator**: Validiert Quest-Header, Schritte und Verweise auf Dialog-Tags.

## Wichtige Gradle-Tasks

```bash
# Quest- und Dialogdaten prüfen
./gradlew :utils:validateQuestContent
```

Der Task erwartet das Verzeichnis `assets/` als Eingabe und prüft dort die Yarn-Quests.
