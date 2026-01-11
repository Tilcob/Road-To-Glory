package com.github.tilcob.game.screen;

import com.badlogic.ashley.core.Engine;
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
import com.github.tilcob.game.system.AiSystem;
import com.github.tilcob.game.system.AnimationSystem;
import com.github.tilcob.game.system.AttackSystem;
import com.github.tilcob.game.system.CameraSystem;
import com.github.tilcob.game.system.ChestSystem;
import com.github.tilcob.game.system.ControllerSystem;
import com.github.tilcob.game.system.DamageSystem;
import com.github.tilcob.game.system.DialogSystem;
import com.github.tilcob.game.system.FacingSystem;
import com.github.tilcob.game.system.FsmSystem;
import com.github.tilcob.game.system.InventorySystem;
import com.github.tilcob.game.system.LifeSystem;
import com.github.tilcob.game.system.MapChangeSystem;
import com.github.tilcob.game.system.PhysicDebugRenderSystem;
import com.github.tilcob.game.system.PhysicMoveSystem;
import com.github.tilcob.game.system.PhysicSystem;
import com.github.tilcob.game.system.QuestSystem;
import com.github.tilcob.game.system.RenderSystem;
import com.github.tilcob.game.system.TriggerSystem;
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

        engine.addSystem(new ControllerSystem(screenNavigator, services.getEventBus()));
        engine.addSystem(new PhysicMoveSystem());
        engine.addSystem(new AttackSystem(physicWorld, services.getAudioManager()));
        engine.addSystem(new FsmSystem());
        engine.addSystem(new AiSystem());
        engine.addSystem(new FacingSystem());
        engine.addSystem(new PhysicSystem(physicWorld, Constants.FIXED_INTERVAL, services.getEventBus()));
        engine.addSystem(new DamageSystem(gameViewModel));
        engine.addSystem(new LifeSystem(gameViewModel));
        engine.addSystem(new AnimationSystem(services.getAssetManager()));
        engine.addSystem(new TriggerSystem(services.getAudioManager(), services.getEventBus()));
        engine.addSystem(new MapChangeSystem(tiledManager, services.getEventBus(), services.getStateManager()));
        engine.addSystem(new InventorySystem(services.getEventBus()));
        engine.addSystem(new ChestSystem());
        engine.addSystem(new QuestSystem(services.getEventBus()));
        //this.engine.addSystem(new DialogRequestSystem());
        engine.addSystem(new DialogSystem(services.getEventBus(), services.getAllDialogs()));
        engine.addSystem(new CameraSystem(camera));
        engine.addSystem(new RenderSystem(batch, viewport, camera));
        engine.addSystem(new PhysicDebugRenderSystem(physicWorld, camera));

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
