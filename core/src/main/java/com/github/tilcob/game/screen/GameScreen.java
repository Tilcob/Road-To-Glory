package com.github.tilcob.game.screen;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tilcob.game.GdxGame;
import com.github.tilcob.game.assets.MapAsset;
import com.github.tilcob.game.assets.SkinAsset;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.component.Controller;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.input.GameControllerState;
import com.github.tilcob.game.input.GameState;
import com.github.tilcob.game.input.KeyboardController;
import com.github.tilcob.game.player.PlayerFactory;
import com.github.tilcob.game.player.PlayerStateApplier;
import com.github.tilcob.game.system.*;
import com.github.tilcob.game.tiled.TiledAshleyConfigurator;
import com.github.tilcob.game.tiled.TiledManager;
import com.github.tilcob.game.ui.model.GameViewModel;
import com.github.tilcob.game.ui.model.InventoryViewModel;
import com.github.tilcob.game.ui.view.GameView;
import com.github.tilcob.game.ui.view.InventoryView;

import java.util.function.Consumer;

public class GameScreen extends ScreenAdapter {
    private final Engine engine;  // Could also be done with PoolingEngine for better performance in Java
    private final TiledManager tiledManager;
    private final TiledAshleyConfigurator tiledAshleyConfigurator;
    private final KeyboardController keyboardController;
    private final GdxGame game;
    private final World physicWorld;
    private final AudioManager audioManager;
    private final Stage stage;
    private final Viewport uiViewport;
    private final GameViewModel gameViewModel;
    private final InventoryViewModel inventoryViewModel;
    private final Skin skin;

    public GameScreen(GdxGame game) {
        this.game = game;
        this.engine = new Engine();
        this.physicWorld = new World(Constants.GRAVITY, true);
        this.physicWorld.setAutoClearForces(false);
        this.tiledManager = new TiledManager(game.getAssetManager(), physicWorld, engine);
        this.tiledAshleyConfigurator = new TiledAshleyConfigurator(engine, game.getAssetManager(), this.physicWorld, game.getChestRegistry(), tiledManager);
        this.keyboardController = new KeyboardController(GameControllerState.class, game.getEventBus(),
            GameState.GAME, engine.getEntitiesFor(Family.all(Controller.class).get()));
        this.audioManager = game.getAudioManager();
        this.uiViewport = new FitViewport(640f, 360f);
        this.stage = new Stage(uiViewport, game.getBatch());
        this.gameViewModel = new GameViewModel(game);
        this.inventoryViewModel = new InventoryViewModel(game);
        this.skin = game.getAssetManager().get(SkinAsset.DEFAULT);

        this.engine.addSystem(new ControllerSystem(game));
        this.engine.addSystem(new PhysicMoveSystem());
        this.engine.addSystem(new AttackSystem(physicWorld, audioManager));
        this.engine.addSystem(new FsmSystem());
        this.engine.addSystem(new FacingSystem());
        this.engine.addSystem(new PhysicSystem(physicWorld, Constants.FIXED_INTERVAL));
        this.engine.addSystem(new DamageSystem(gameViewModel));
        this.engine.addSystem(new LifeSystem(gameViewModel));
        this.engine.addSystem(new AnimationSystem(game.getAssetManager()));
        this.engine.addSystem(new TriggerSystem(audioManager));
        this.engine.addSystem(new MapChangeSystem(tiledManager, game.getStateManager()));
        this.engine.addSystem(new InventorySystem(game.getEventBus()));
        this.engine.addSystem(new ChestSystem());
        this.engine.addSystem(new CameraSystem(game.getCamera()));
        this.engine.addSystem(new RenderSystem(game.getBatch(), game.getViewport(), game.getCamera()));
        this.engine.addSystem(new PhysicDebugRenderSystem(physicWorld, game.getCamera()));
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        uiViewport.update(width, height, true);
    }

    @Override
    public void show() {
        game.setInputProcessors(stage, keyboardController);
        keyboardController.setActiveState(GameControllerState.class);

        stage.addActor(new GameView(skin, stage, gameViewModel));
        stage.addActor(new InventoryView(skin, stage, inventoryViewModel));

        Consumer<TiledMap> renderConsumer = engine.getSystem(RenderSystem.class)::setMap;
        Consumer<TiledMap> cameraConsumer = engine.getSystem(CameraSystem.class)::setMap;
        Consumer<TiledMap> audioConsumer = audioManager::setMap;

        tiledManager.setMapChangeConsumer(renderConsumer.andThen(cameraConsumer).andThen(audioConsumer));
        tiledManager.setLoadObjectConsumer(tiledAshleyConfigurator::onLoadObject);
        tiledManager.setLoadTileConsumer(tiledAshleyConfigurator::onLoadTile);
        tiledManager.setLoadTriggerConsumer(tiledAshleyConfigurator::onLoadTrigger);

        createPlayerFromState();
    }

    private void createPlayerFromState() {
        Entity player = PlayerFactory.create(engine, game.getAssetManager(), physicWorld);
        loadMap();

        if (game.getStateManager().getGameState().getPlayerState() != null) {
            PlayerStateApplier.apply(game.getStateManager().getGameState().getPlayerState(), player);
        } else {
            Transform.MAPPER.get(player).getPosition().set(tiledManager.getSpawnPoint());
        }
    }

    private void loadMap() {
        MapAsset mapToLoad = game.getStateManager().getGameState().getCurrentMap();
        if (mapToLoad == null) mapToLoad = MapAsset.MAIN;
        tiledManager.setMap(tiledManager.loadMap(mapToLoad));
    }

    @Override
    public void hide() {
        engine.removeAllEntities();
        stage.clear();
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1 / 30f);
        engine.update(delta);

        uiViewport.apply();
        stage.getBatch().setColor(Color.WHITE);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        for (EntitySystem system : engine.getSystems()) {
            if (system instanceof Disposable disposable) {
                disposable.dispose();
            }
        }
        gameViewModel.dispose();
        physicWorld.dispose();
        stage.dispose();
    }
}
