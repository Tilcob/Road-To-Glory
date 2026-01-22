# Core Module

This module contains the platform-independent game logic for **Road-To-Glory**. It houses the ECS components and systems, UI views, dialog and quest logic, and core services such as savegame handling and asset management.

## Focus areas

- **ECS (Ashley)**: Components and systems for gameplay, AI, rendering, and interactions.
- **Dialog & quests**: Yarn-based dialogs, quest definitions, validation.
- **Cutscenes**: Yarn-driven scripted sequences that integrate with camera, movement, and dialogs.
- **UI**: Scene2D UI layouts and widgets.
- **Services**: Savegame, registries, resource management.
- **Debugging**: Debug overlay with runtime stats and recent log buffer when `game.debug` is enabled.

## Systems (`com.github.tilcob.game.system`)

- `AiSystem`: Drives AI updates.
- `AnimationSystem`: Updates animations.
- `AttackSystem`: Processes attacks.
- `CameraSystem`: Maintains camera position and state.
- `ChestSystem`: Handles chest interactions.
- `ControllerSystem`: Handles input/controller events.
- `CutsceneSystem`: Plays Yarn-based cutscenes and waits for dialog, camera, and movement gates.
- `DamageSystem`: Applies damage.
- `DialogConsequenceSystem`: Applies dialog consequences.
- `DialogQuestBridgeSystem`: Bridges dialogs and quests.
- `DialogSystem`: Dialog flow and display.
- `EquipmentStatModifierSystem`: Applies equipment modifiers to stats.
- `EquipmentSystem`: Manages equipment.
- `FacingSystem`: Updates facing a direction.
- `FsmSystem`: Updates FSM logic.
- `InventorySystem`: Inventory logic.
- `LevelUpSystem`: Level-up logic.
- `LifeSystem`: Health logic.
- `MapChangeSystem`: Map changes and transitions.
- `MoveIntentSystem`: Applies `MoveIntent` movement for non-NPC entities.
- `NpcPathfindingSystem`: NPC pathfinding.
- `PhysicDebugRenderSystem`: Physics debug rendering.
- `PhysicMoveSystem`: Physics-based movement logic.
- `PhysicSystem`: Physics stepping/simulation.
- `QuestRewardSchedulerSystem`: Schedules quest rewards.
- `QuestSystem`: Quest state and progress.
- `RenderSystem`: Renders the game world.
- `RewardSystem`: Applies rewards.
- `StatModifierDurationSystem`: Time-based stat modifiers.
- `StatRecalcSystem`: Stat recalculation.
- `TriggerSystem`: Trigger events.

## Important dependencies

- LibGDX (Core, Box2D, Freetype)
- Ashley (ECS)
- GDX-AI, Box2DLights
- YarnGdx for dialogs

## Tests

Tests run on the LibGDX headless backend and use the assets as the working directory:

```bash
./gradlew core:test
```

## Notes on structure

High-level package documentation lives in `package-info.java` inside the source tree. New subsystems should be described there as well.

## Debug overlay

When `game.debug` is enabled, the game installs a debug log buffer and shows a debug overlay 
with FPS, entity/system counts, save slot, and recent log lines. This is intended for development 
builds and mirrors the existing `Gdx.app` logger output.
