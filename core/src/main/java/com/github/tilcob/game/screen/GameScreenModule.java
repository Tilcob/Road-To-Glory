package com.github.tilcob.game.screen;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.assets.SkinAsset;
import com.github.tilcob.game.component.Controller;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.input.GameControllerState;
import com.github.tilcob.game.input.GameState;
import com.github.tilcob.game.input.KeyboardController;
import com.github.tilcob.game.system.*;
import com.github.tilcob.game.tiled.TiledAshleyConfigurator;
import com.github.tilcob.game.tiled.TiledManager;
import com.github.tilcob.game.ui.model.GameViewModel;
import com.github.tilcob.game.ui.model.InventoryViewModel;

public class GameScreenModule {
    private final GameServices services;
    private final Batch batch;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final InputMultiplexer inputMultiplexer;
    private final ScreenNavigator screenNavigator;

    public GameScreenModule(
        GameServices services,
        Batch batch,
        OrthographicCamera camera,
        Viewport viewport,
        InputMultiplexer inputMultiplexer,
        ScreenNavigator screenNavigator
    ) {
        this.services = services;
        this.batch = batch;
        this.camera = camera;
        this.viewport = viewport;
        this.inputMultiplexer = inputMultiplexer;
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
        KeyboardController keyboardController = new KeyboardController(
            GameControllerState.class,
            services.getEventBus(),
            GameState.GAME,
            engine.getEntitiesFor(Family.all(Controller.class).get())
        );
        Stage stage = new Stage(uiViewport, batch);
        GameViewModel gameViewModel = new GameViewModel(services, viewport);
        InventoryViewModel inventoryViewModel = new InventoryViewModel(services);
        Skin skin = services.getAssetManager().get(SkinAsset.DEFAULT);

        // Input
        engine.addSystem(withPriority(
            new ControllerSystem(screenNavigator, services.getEventBus()),
            SystemOrder.INPUT
        ));

        // AI
        engine.addSystem(withPriority(new FsmSystem(), SystemOrder.AI));
        engine.addSystem(withPriority(new AiSystem(), SystemOrder.AI));

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
        engine.addSystem(withPriority(new DamageSystem(gameViewModel), SystemOrder.COMBAT));
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
        engine.addSystem(withPriority(new InventorySystem(services.getEventBus()), SystemOrder.GAMEPLAY));
        engine.addSystem(withPriority(new ChestSystem(), SystemOrder.GAMEPLAY));
        engine.addSystem(withPriority(new QuestSystem(services.getEventBus()), SystemOrder.GAMEPLAY));
        //this.engine.addSystem(new DialogRequestSystem());
        engine.addSystem(withPriority(
            new DialogSystem(services.getEventBus(), services.getAllDialogs()),
            SystemOrder.GAMEPLAY
        ));

        // Render
        engine.addSystem(withPriority(new AnimationSystem(services.getAssetManager()), SystemOrder.RENDER));
        engine.addSystem(withPriority(new CameraSystem(camera), SystemOrder.RENDER));
        engine.addSystem(withPriority(new RenderSystem(batch, viewport, camera), SystemOrder.RENDER));
        engine.addSystem(withPriority(
            new PhysicDebugRenderSystem(physicWorld, camera),
            SystemOrder.DEBUG_RENDER
        ));

        return new Dependencies(
            engine,
            tiledManager,
            tiledAshleyConfigurator,
            keyboardController,
            physicWorld,
            stage,
            gameViewModel,
            inventoryViewModel,
            skin,
            inputMultiplexer,
            services.getAudioManager()
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
        KeyboardController keyboardController,
        World physicWorld,
        Stage stage,
        GameViewModel gameViewModel,
        InventoryViewModel inventoryViewModel,
        Skin skin,
        InputMultiplexer inputMultiplexer,
        com.github.tilcob.game.audio.AudioManager audioManager
    ) {}
}
