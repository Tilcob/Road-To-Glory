package com.github.tilcob.game.screen;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
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
import com.github.tilcob.game.input.*;
import com.github.tilcob.game.system.*;
import com.github.tilcob.game.tiled.TiledAshleyConfigurator;
import com.github.tilcob.game.tiled.TiledManager;
import com.github.tilcob.game.ui.model.GameViewModel;
import com.github.tilcob.game.ui.model.InventoryViewModel;
import com.github.tilcob.game.ui.model.PauseViewModel;

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
        ScreenNavigator screenNavigator
    ) {
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
        World physicWorld = new World(Constants.GRAVITY, true);
        physicWorld.setAutoClearForces(false);
        TiledManager tiledManager = new TiledManager(services.getAssetManager(), physicWorld, engine);
        TiledAshleyConfigurator tiledAshleyConfigurator = new TiledAshleyConfigurator(
            engine,
            services.getAssetManager(),
            physicWorld,
            services.getChestRegistry(),
            tiledManager
        );
        IdleControllerState idleControllerState = new IdleControllerState();
        ActiveEntityReference activeEntityReference = new ActiveEntityReference();
        GameControllerState gameControllerState = new GameControllerState(activeEntityReference);
        UiControllerState uiControllerState = new UiControllerState(services.getEventBus());
        Stage stage = new Stage(uiViewport, batch);
        GameViewModel gameViewModel = new GameViewModel(services, viewport);
        InventoryViewModel inventoryViewModel = new InventoryViewModel(services);
        PauseViewModel pauseViewModel = new PauseViewModel(services, screenNavigator);
        Skin skin = services.getAssetManager().get(SkinAsset.DEFAULT);

        // Input
        engine.addSystem(withPriority(
            new ControllerSystem(services.getEventBus()),
            SystemOrder.INPUT
        ));

        // AI
        engine.addSystem(withPriority(new FsmSystem(), SystemOrder.AI));
        engine.addSystem(withPriority(new AiSystem(), SystemOrder.AI));
        engine.addSystem(withPriority(new NpcPathfindingSystem(), SystemOrder.AI));

        // Physics
        engine.addSystem(withPriority(new PhysicMoveSystem(), SystemOrder.PHYSICS));
        engine.addSystem(withPriority(new FacingSystem(), SystemOrder.PHYSICS));
        engine.addSystem(withPriority(
            new PhysicSystem(physicWorld, Constants.FIXED_INTERVAL, services.getEventBus()),
            SystemOrder.PHYSICS
        ));

        // Combat
        engine.addSystem(withPriority(
            new AttackSystem(physicWorld, services.getAudioManager()),
            SystemOrder.COMBAT
        ));
        engine.addSystem(withPriority(
            new DamageSystem(gameViewModel, services.getQuestManager()),
            SystemOrder.COMBAT
        ));
        engine.addSystem(withPriority(new LifeSystem(gameViewModel), SystemOrder.COMBAT));
        engine.addSystem(withPriority(
            new TriggerSystem(services.getAudioManager(), services.getEventBus()),
            SystemOrder.COMBAT
        ));

        // Gameplay & UI
        engine.addSystem(withPriority(
            new MapChangeSystem(tiledManager, services.getEventBus(), services.getStateManager()),
            SystemOrder.GAMEPLAY
        ));
        engine.addSystem(withPriority(
            new InventorySystem(services.getEventBus(), services.getQuestManager()),
            SystemOrder.GAMEPLAY
        ));
        engine.addSystem(withPriority(new ChestSystem(), SystemOrder.GAMEPLAY));
        engine.addSystem(withPriority(new QuestSystem(
            services.getEventBus(), services.getQuestLifecycleService()),
            SystemOrder.GAMEPLAY));
        engine.addSystem(withPriority(new RewardSystem(services.getEventBus(), services.getQuestLifecycleService()),
            SystemOrder.GAMEPLAY));
        engine.addSystem(withPriority(
            new QuestRewardSchedulerSystem(
                services.getEventBus(),
                services.getQuestLifecycleService()
            ),
            SystemOrder.GAMEPLAY
        ));
        engine.addSystem(withPriority(new DialogConsequenceSystem(
                services.getEventBus(),
                services.getQuestManager(),
                services.getQuestLifecycleService()
            ),
            SystemOrder.GAMEPLAY));
        engine.addSystem(withPriority(
            new DialogQuestBridgeSystem(
                services.getEventBus(),
                services.getQuestManager()
            ),
            SystemOrder.GAMEPLAY
        ));
        engine.addSystem(withPriority(
            new DialogSystem(services.getEventBus(), services.getAllDialogs(), services.getDialogYarnRuntime()),
            SystemOrder.GAMEPLAY
        ));

        // Render
        engine.addSystem(withPriority(new AnimationSystem(services.getAssetManager()), SystemOrder.RENDER));
        engine.addSystem(withPriority(new CameraSystem(camera), SystemOrder.RENDER));
        engine.addSystem(withPriority(new RenderSystem(batch, viewport, camera), SystemOrder.RENDER));
        if (Constants.DEBUG) {
            engine.addSystem(withPriority(
                new PhysicDebugRenderSystem(physicWorld, camera),
                SystemOrder.DEBUG_RENDER
            ));
        }

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
            pauseViewModel,
            skin,
            inputManager,
            services.getAudioManager(),
            activeEntityReference
        );
    }

    public static <T extends EntitySystem> T withPriority(T system, SystemOrder order) {
        system.priority = order.priority();
        return system;
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
        PauseViewModel pauseViewModel,
        Skin skin,
        InputManager inputManager,
        AudioManager audioManager,
        ActiveEntityReference activeEntityReference
    ) {}
}
