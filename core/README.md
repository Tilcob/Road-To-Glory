# Core-Modul

Dieses Modul enthält die plattformunabhängige Spiellogik für **Road-To-Glory**. Hier liegen die ECS-Komponenten und -Systeme, UI-Ansichten, Dialog- und Quest-Logik sowie zentrale Dienste wie Savegame-Handling und Asset-Management.

## Schwerpunkte

- **ECS (Ashley)**: Komponenten und Systeme für Gameplay, KI, Rendering und Interaktionen.
- **Dialog & Quests**: Yarn-basierte Dialoge, Questdefinitionen, Validierung.
- **UI**: Scene2D UI-Layouts und Widgets.
- **Services**: Savegame, Registry, Ressourcenverwaltung.

## Systeme (com.github.tilcob.game.system)

- `AiSystem`: Steuert KI-Aktualisierungen.
- `AnimationSystem`: Aktualisiert Animationen.
- `AttackSystem`: Verarbeitet Angriffe.
- `CameraSystem`: Pflegt Kameraposition und -zustand.
- `ChestSystem`: Verarbeitet Truhen-Interaktionen.
- `ControllerSystem`: Verarbeitet Eingaben/Controller-Events.
- `DamageSystem`: Wendet Schaden an.
- `DialogConsequenceSystem`: Führt Dialogkonsequenzen aus.
- `DialogQuestBridgeSystem`: Brücke zwischen Dialogen und Quests.
- `DialogSystem`: Dialogfluss und Anzeige.
- `EquipmentStatModifierSystem`: Wendet Ausrüstungs-Modifikatoren auf Werte an.
- `EquipmentSystem`: Verwaltung von Ausrüstung.
- `FacingSystem`: Aktualisiert Blickrichtung.
- `FsmSystem`: Aktualisiert FSM-Logik.
- `InventorySystem`: Inventar-Logik.
- `LevelUpSystem`: Level-Up-Logik.
- `LifeSystem`: Lebenspunkte-Logik.
- `MapChangeSystem`: Kartenwechsel und Übergänge.
- `NpcPathfindingSystem`: Pfadfindung für NPCs.
- `PhysicDebugRenderSystem`: Debug-Rendering für Physik.
- `PhysicMoveSystem`: Bewegungslogik basierend auf Physik.
- `PhysicSystem`: Physik-Schritt/Simulation.
- `QuestRewardSchedulerSystem`: Plant Quest-Belohnungen ein.
- `QuestSystem`: Quest-Zustand und Fortschritt.
- `RenderSystem`: Rendering der Spielwelt.
- `RewardSystem`: Ausführen von Belohnungen.
- `StatModifierDurationSystem`: Zeitbasierte Stat-Modifikatoren.
- `StatRecalcSystem`: Neuberechnung von Stats.
- `TriggerSystem`: Trigger-Events.

## Wichtige Abhängigkeiten

- LibGDX (Core, Box2D, Freetype)
- Ashley (ECS)
- GDX-AI, Box2DLights
- YarnGdx für Dialoge

## Tests

Tests laufen im Headless-Backend von LibGDX und verwenden die Assets als Working Directory:

```bash
./gradlew core:test
```

## Hinweise zur Struktur

Die High-Level-Dokumentation der Packages befindet sich in `package-info.java` innerhalb des Quellbaums. Neue Subsysteme sollten dort ebenfalls beschrieben werden.
