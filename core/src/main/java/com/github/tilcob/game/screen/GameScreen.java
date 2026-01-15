package com.github.tilcob.game.screen;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.assets.MapAsset;
import com.github.tilcob.game.component.DialogFlags;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.event.AutosaveEvent;
import com.github.tilcob.game.event.MapChangeEvent;
import com.github.tilcob.game.event.PauseEvent;
import com.github.tilcob.game.event.UpdateInventoryEvent;
import com.github.tilcob.game.input.*;
import com.github.tilcob.game.player.PlayerFactory;
import com.github.tilcob.game.player.PlayerStateApplier;
import com.github.tilcob.game.quest.QuestFactory;
import com.github.tilcob.game.quest.QuestLoader;
import com.github.tilcob.game.system.CameraSystem;
import com.github.tilcob.game.system.RenderSystem;
import com.github.tilcob.game.tiled.TiledAshleyConfigurator;
import com.github.tilcob.game.tiled.TiledManager;
import com.github.tilcob.game.ui.model.GameViewModel;
import com.github.tilcob.game.ui.model.InventoryViewModel;
import com.github.tilcob.game.ui.model.PauseViewModel;
import com.github.tilcob.game.ui.view.GameView;
import com.github.tilcob.game.ui.view.InventoryView;
import com.github.tilcob.game.ui.view.PauseView;

import java.util.function.Consumer;

public class GameScreen extends ScreenAdapter {
    private final GameServices services;
    private final Viewport uiViewport;
    private Engine engine;
    private TiledManager tiledManager;
    private TiledAshleyConfigurator tiledAshleyConfigurator;
    private IdleControllerState idleControllerState;
    private GameControllerState gameControllerState;
    private UiControllerState uiControllerState;
    private World physicWorld;
    private com.github.tilcob.game.audio.AudioManager audioManager;
    private Stage stage;
    private GameViewModel gameViewModel;
    private InventoryViewModel inventoryViewModel;
    private PauseViewModel pauseViewModel;
    private Skin skin;
    private InputManager inputManager;
    private Entity player;
    private ActiveEntityReference activeEntityReference;
    private GameView gameView;
    private PauseView pauseView;
    private boolean paused;

    public GameScreen(GameServices services, Viewport uiViewport) {
        this.services = services;
        this.uiViewport = uiViewport;
    }

    void initialize(GameScreenModule.Dependencies dependencies) {
        this.engine = dependencies.engine();
        this.tiledManager = dependencies.tiledManager();
        this.tiledAshleyConfigurator = dependencies.tiledAshleyConfigurator();
        this.idleControllerState = dependencies.idleControllerState();
        this.gameControllerState = dependencies.gameControllerState();
        this.uiControllerState = dependencies.uiControllerState();
        this.physicWorld = dependencies.physicWorld();
        this.audioManager = dependencies.audioManager();
        this.stage = dependencies.stage();
        this.gameViewModel = dependencies.gameViewModel();
        this.inventoryViewModel = dependencies.inventoryViewModel();
        this.pauseViewModel = dependencies.pauseViewModel();
        this.skin = dependencies.skin();
        this.inputManager = dependencies.inputManager();
        this.activeEntityReference = dependencies.activeEntityReference();

        services.getEventBus().subscribe(AutosaveEvent.class, this::autosave);
        services.getEventBus().subscribe(PauseEvent.class, this::togglePause);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        uiViewport.update(width, height, true);
    }

    @Override
    public void show() {
        inputManager.setInputProcessors(stage);
        inputManager.configureStates(GameControllerState.class, idleControllerState, gameControllerState, uiControllerState);

        gameView = new GameView(skin, stage, gameViewModel);
        stage.addActor(gameView);
        stage.addActor(new InventoryView(skin, stage, inventoryViewModel));
        pauseView = new PauseView(skin, stage, pauseViewModel);
        pauseView.setVisible(false);
        stage.addActor(pauseView);
        paused = false;


        Consumer<TiledMap> renderConsumer = engine.getSystem(RenderSystem.class)::setMap;
        Consumer<TiledMap> cameraConsumer = engine.getSystem(CameraSystem.class)::setMap;
        Consumer<TiledMap> audioConsumer = audioManager::setMap;

        tiledManager.setMapChangeConsumer(renderConsumer.andThen(cameraConsumer).andThen(audioConsumer));
        tiledManager.setLoadObjectConsumer(tiledAshleyConfigurator::onLoadObject);
        tiledManager.setLoadTileConsumer(tiledAshleyConfigurator::onLoadTile);
        tiledManager.setLoadTriggerConsumer(tiledAshleyConfigurator::onLoadTrigger);

        createPlayer();
        loadQuest();
    }

    private void loadQuest() {
        QuestLoader loader = new QuestLoader(new QuestFactory(services.getEventBus()));
        QuestLog questLog = QuestLog.MAPPER.get(player);
        services.getStateManager().loadQuests(questLog, loader);
    }

    private void createPlayer() {
        player = PlayerFactory.create(engine, services.getAssetManager(), physicWorld);
        activeEntityReference.set(player);
        loadMap();

        if (services.getStateManager().getGameState().getPlayerState() != null) {
            PlayerStateApplier.apply(services.getStateManager().getGameState().getPlayerState(), player);
        } else {
            Transform.MAPPER.get(player).getPosition().set(tiledManager.getSpawnPoint());
        }
        services.getStateManager().loadDialogFlags(DialogFlags.MAPPER.get(player));
        services.getStateManager().setPlayerState(player);
        services.getEventBus().fire(new UpdateInventoryEvent(player));
    }

    private void loadMap() {
        MapAsset mapToLoad = services.getStateManager().getGameState().getCurrentMap();
        if (mapToLoad == null) mapToLoad = MapAsset.MAIN;
        tiledManager.setMap(tiledManager.loadMap(mapToLoad));
        services.getEventBus().fire(new MapChangeEvent(tiledManager.getCurrentMapAsset().name().toLowerCase()));
    }

    @Override
    public void hide() {
        engine.removeAllEntities();
        stage.clear();
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1 / 30f);
        if (!paused) engine.update(delta);

        uiViewport.apply();
        stage.getBatch().setColor(Color.WHITE);
        stage.act(delta);
        stage.draw();
    }

    private void autosave(AutosaveEvent event) {
        if (player == null) return;
        services.getStateManager().saveQuests(QuestLog.MAPPER.get(player));
        services.getStateManager().saveDialogFlags(DialogFlags.MAPPER.get(player));
        services.getStateManager().setPlayerState(player);
        services.saveGame();
    }

    private void togglePause(PauseEvent event) {
        if (event == null) return;
        switch (event.action()) {
            case PAUSE -> setPaused(true);
            case RESUME -> setPaused(false);
            case TOGGLE -> setPaused(!paused);
        }
    }

    private void setPaused(boolean paused) {
        if (this.paused == paused) {
            return;
        }
        this.paused = paused;
        pauseView.setVisible(paused);
        gameView.setVisible(!paused);
        if (paused) {
            pauseView.toFront();
            inputManager.setActiveState(UiControllerState.class);
        } else {
            inputManager.setActiveState(GameControllerState.class);
        }
    }

    @Override
    public void dispose() {
        for (EntitySystem system : engine.getSystems()) {
            if (system instanceof Disposable disposable) {
                disposable.dispose();
            }
        }
        services.getEventBus().unsubscribe(AutosaveEvent.class, this::autosave);
        services.getEventBus().unsubscribe(PauseEvent.class, this::togglePause);
        gameViewModel.dispose();
        pauseViewModel.dispose();
        physicWorld.dispose();
        stage.dispose();
    }
}
