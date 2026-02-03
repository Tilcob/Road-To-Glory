package com.github.tilcob.game.screen;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.assets.SkinAsset;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.entity.EngineEntityLookup;
import com.github.tilcob.game.entity.EntityIdService;
import com.github.tilcob.game.input.*;
import com.github.tilcob.game.inventory.InventoryService;
import com.github.tilcob.game.skill.ExpDistributionLoader;
import com.github.tilcob.game.skill.SkillTreeLoader;
import com.github.tilcob.game.system.installers.*;
import com.github.tilcob.game.tiled.TiledAshleyConfigurator;
import com.github.tilcob.game.tiled.TiledManager;
import com.github.tilcob.game.ui.UiModelFactory;
import com.github.tilcob.game.ui.model.*;
import com.github.tilcob.game.ui.view.SkillTreeView;

public class GameScreenModule {
        private final GameServices services;
        private final Batch batch;
        private final OrthographicCamera camera;
        private final Viewport viewport;
        private final InputManager inputManager;
        private final ScreenNavigator screenNavigator;

        public GameScreenModule(
                        GameServices services,
                        Batch batch,
                        OrthographicCamera camera,
                        Viewport viewport,
                        InputManager inputManager,
                        ScreenNavigator screenNavigator) {
                this.services = services;
                this.batch = batch;
                this.camera = camera;
                this.viewport = viewport;
                this.inputManager = inputManager;
                this.screenNavigator = screenNavigator;
        }

        public GameScreen create(Viewport uiViewport) {
                GameScreen screen = new GameScreen(services, uiViewport);
                screen.initialize(createDependencies(uiViewport));
                return screen;
        }

        private Dependencies createDependencies(Viewport uiViewport) {
                Engine engine = new Engine();
                EntityIdService entityIdService = new EntityIdService(engine);
                services.setEntityLookup(new EngineEntityLookup(engine, entityIdService));
                SkillTreeLoader.loadAll();
                ExpDistributionLoader.loadAll();
                World physicWorld = new World(Constants.GRAVITY, true);
                physicWorld.setAutoClearForces(false);
                TiledManager tiledManager = new TiledManager(services.getAssetManager(), physicWorld, engine);
                TiledAshleyConfigurator tiledAshleyConfigurator = new TiledAshleyConfigurator(
                                engine,
                                services.getAssetManager(),
                                physicWorld,
                                services.getChestRegistry(),
                                tiledManager);
                IdleControllerState idleControllerState = new IdleControllerState();
                ActiveEntityReference activeEntityReference = new ActiveEntityReference();
                services.setActiveEntityReference(activeEntityReference);
                GameControllerState gameControllerState = new GameControllerState(activeEntityReference);
                UiControllerState uiControllerState = new UiControllerState(services.getEventBus());
                Stage stage = new Stage(uiViewport, batch);
                UiModelFactory uiModelFactory = new UiModelFactory();
                GameViewModel gameViewModel = new GameViewModel(services, viewport, uiModelFactory);
                InventoryViewModel inventoryViewModel = new InventoryViewModel(services, uiModelFactory);
                ChestViewModel chestViewModel = new ChestViewModel(services, uiModelFactory);
                PauseViewModel pauseViewModel = new PauseViewModel(services, screenNavigator);
                SettingsViewModel settingsViewModel = new SettingsViewModel(services, inputManager);
                com.github.tilcob.game.ui.model.SkillTreeViewModel skillTreeViewModel = new com.github.tilcob.game.ui.model.SkillTreeViewModel(
                                services);
                Skin skin = services.getAssetManager().get(SkinAsset.DEFAULT);
                services.getInventoryService().setSkin(skin);
                services.getInventoryService().setEngine(engine);

                // Installers
                new InputSystemsInstaller(services.getEventBus()).install(engine);
                new AiSystemsInstaller().install(engine);
                new PhysicsSystemsInstaller(physicWorld, services.getEventBus()).install(engine);
                new CombatSystemsInstaller(
                                physicWorld,
                                services.getAudioManager(),
                                services.getEventBus(),
                                gameViewModel,
                                services.getQuestManager(),
                                screenNavigator).install(engine);
                new GameplaySystemsInstaller(
                                tiledManager,
                                services.getEventBus(),
                                services.getStateManager(),
                                services.getQuestManager(),
                                services.getInventoryService(),
                                services.getQuestLifecycleService(),
                                services.getQuestRewardService(),
                                services.getDialogYarnRuntime(),
                                services.getCutsceneYarnRuntime(),
                                services.getQuestYarnRuntime(),
                                services.getAllDialogs(),
                                services.getAllCutscenes()).install(engine);

                new RenderSystemsInstaller(
                                batch,
                                viewport,
                                camera,
                                services.getAssetManager(),
                                physicWorld,
                                Constants.DEBUG).install(engine);

                SkillTreeView skillTreeView = new SkillTreeView(skin, stage, skillTreeViewModel);
                return new Dependencies(
                                engine,
                                tiledManager,
                                tiledAshleyConfigurator,
                                idleControllerState,
                                gameControllerState,
                                uiControllerState,
                                physicWorld,
                                stage,
                                gameViewModel,
                                inventoryViewModel,
                                chestViewModel,
                                pauseViewModel,
                                settingsViewModel,
                                skillTreeViewModel,
                                skillTreeView,
                                skin,
                                inputManager,
                                services.getAudioManager(),
                                activeEntityReference,
                                services.getInventoryService());
        }

        public record Dependencies(
                        Engine engine,
                        TiledManager tiledManager,
                        TiledAshleyConfigurator tiledAshleyConfigurator,
                        IdleControllerState idleControllerState,
                        GameControllerState gameControllerState,
                        UiControllerState uiControllerState,
                        World physicWorld,
                        Stage stage,
                        GameViewModel gameViewModel,
                        InventoryViewModel inventoryViewModel,
                        ChestViewModel chestViewModel,
                        PauseViewModel pauseViewModel,
                        SettingsViewModel settingsViewModel,
                        SkillTreeViewModel skillTreeViewModel,
                        SkillTreeView skillTreeView,
                        Skin skin,
                        InputManager inputManager,
                        AudioManager audioManager,
                        ActiveEntityReference activeEntityReference,
                        InventoryService inventoryService) {
        }
}
