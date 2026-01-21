# Assets

Dieses Verzeichnis enthält die zur Laufzeit benötigten Assets für **Road-To-Glory**. Die Dateien werden vom Spiel geladen und in der Desktop-App direkt als Ressourcen genutzt.

## Struktur (Auszug)

- `audio/`: Soundeffekte und Musik.
- `graphics/`: Texturen, Spritesheets und Grafiken.
- `maps/`: Karten- und Tilemap-Daten.
- `dialogs/`: Dialogdateien (Yarn).
- `quests/`: Questdefinitionen und `index.json`.
- `items/`: Item-Definitionen.
- `ui/`: UI-Skins, Layouts und UI-Assets.
- `stats/`: Werte- und Balancing-Daten.
- `tests/`: Test-/Beispieldaten für Content.

## Hinweise

- `quests/index.json` wird aus den `.yarn`-Dateien generiert.
- Das Spiel startet mit `assets/` als Working Directory (z. B. über `lwjgl3:run`).
