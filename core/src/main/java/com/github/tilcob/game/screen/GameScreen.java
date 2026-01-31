package com.github.tilcob.game.screen;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.assets.MapAsset;
import com.github.tilcob.game.assets.SkinAsset;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.debug.ContentHotReload;
import com.github.tilcob.game.debug.ContentReloadService;
import com.github.tilcob.game.event.*;
import com.github.tilcob.game.input.*;
import com.github.tilcob.game.player.PlayerFactory;
import com.github.tilcob.game.player.PlayerStateApplier;
import com.github.tilcob.game.quest.QuestFactory;
import com.github.tilcob.game.quest.QuestLoader;
import com.github.tilcob.game.system.CameraSystem;
import com.github.tilcob.game.system.RenderSystem;
import com.github.tilcob.game.tiled.TiledAshleyConfigurator;
import com.github.tilcob.game.tiled.TiledManager;
import com.github.tilcob.game.ui.GameUiBuilder;
import com.github.tilcob.game.ui.model.*;
import com.github.tilcob.game.ui.view.*;

import java.util.function.Consumer;

import static com.github.tilcob.game.event.UiOverlayEvent.Type.*;

public class GameScreen extends ScreenAdapter {
    private final GameServices services;
    private final Viewport uiViewport;
    private final Group gameUiGroup;
    private final GameUiBuilder gameUiBuilder = new GameUiBuilder();
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
    private ChestViewModel chestViewModel;
    private PauseViewModel pauseViewModel;
    private SettingsViewModel settingsViewModel;
    private Skin skin;
    private InputManager inputManager;
    private Entity player;
    private ActiveEntityReference activeEntityReference;
    private PauseView pauseView;
    private SettingsView settingsView;
    private DebugOverlayView debugOverlayView;
    private boolean paused;
    private ContentReloadService contentReloadService;
    private ContentHotReload contentHotReload;
    private SettingsOverlayController settingsOverlayController;

    public GameScreen(GameServices services, Viewport uiViewport) {
        this.services = services;
        this.uiViewport = uiViewport;
        this.gameUiGroup = new Group();
        this.gameUiGroup.setSize(uiViewport.getWorldWidth(), uiViewport.getWorldHeight());
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
        this.chestViewModel = dependencies.chestViewModel();
        this.pauseViewModel = dependencies.pauseViewModel();
        this.settingsViewModel = dependencies.settingsViewModel();
        this.skin = dependencies.skin();
        this.inputManager = dependencies.inputManager();
        this.activeEntityReference = dependencies.activeEntityReference();

        services.getEventBus().subscribe(AutosaveEvent.class, this::autosave);
        services.getEventBus().subscribe(PauseEvent.class, this::togglePause);
        services.getUiServices().setSkin(skin);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        uiViewport.update(width, height, true);
        gameUiGroup.setSize(uiViewport.getWorldWidth(), uiViewport.getWorldHeight());
    }

    @Override
    public void show() {
        inputManager.setInputProcessors(stage);
        inputManager.configureStates(GameControllerState.class, idleControllerState, gameControllerState, uiControllerState);
        clearAllControllerCommands();

        stage.addActor(gameUiGroup);
        gameUiBuilder.buildGameUi(gameUiGroup, buildUiDependencies());

        if (Constants.DEBUG) {
            contentReloadService = new ContentReloadService(services);
            contentHotReload = new ContentHotReload(contentReloadService.collectWatchFiles(), 1f);
        }

        GameUiBuilder.OverlayViews overlays = gameUiBuilder.buildOverlays(
            stage,
            buildUiDependencies(),
            paused,
            false
        );
        pauseView = overlays.pauseView();
        settingsView = overlays.settingsView();
        debugOverlayView = overlays.debugOverlayView();
        settingsOverlayController = new SettingsOverlayController(
            stage,
            pauseView,
            settingsView,
            pauseView::selectSettings,
            settingsView::resetSelection
        );
        setPaused(false);
        services.getEventBus().fire(new UiOverlayEvent(UiOverlayEvent.Type.CLOSE_SETTINGS));

        Consumer<TiledMap> renderConsumer = engine.getSystem(RenderSystem.class)::setMap;
        Consumer<TiledMap> cameraConsumer = engine.getSystem(CameraSystem.class)::setMap;
        Consumer<TiledMap> audioConsumer = audioManager::setMap;

        tiledManager.setMapChangeConsumer(renderConsumer.andThen(cameraConsumer).andThen(audioConsumer));
        tiledManager.setLoadObjectConsumer(tiledAshleyConfigurator::onLoadObject);
        tiledManager.setLoadTileConsumer(tiledAshleyConfigurator::onLoadTile);
        tiledManager.setLoadTriggerConsumer(tiledAshleyConfigurator::onLoadTrigger);

        createPlayer();
        loadQuest();
        services.getEventBus().subscribe(UiOverlayEvent.class, this::onOverlayEvent);
    }

    private void loadQuest() {
        QuestLoader loader = new QuestLoader(new QuestFactory(services.getQuestYarnRegistry()));
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
            Physic.MAPPER.get(player).getBody().setTransform(tiledManager.getSpawnPoint(), 0);
        }
        services.getStateManager().loadDialogFlags(DialogFlags.MAPPER.get(player));
        services.getStateManager().loadCounters(Counters.MAPPER.get(player));
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
        services.getEventBus().unsubscribe(UiOverlayEvent.class, this::onOverlayEvent);

        if (settingsViewModel != null) {
            settingsViewModel.dispose();
            settingsViewModel = null;
        }
    }

    private void onOverlayEvent(UiOverlayEvent event) {
        if (event == null || !paused) return;

        switch (event.type()) {
            case OPEN_SETTINGS, TOGGLE_SETTINGS -> {
                boolean willOpen = (event.type() == OPEN_SETTINGS) || !settingsViewModel.isOpen();
                if (willOpen) {
                    openSettingsOverlay();
                } else {
                    closeSettingsOverlay();
                }
            }
            case CLOSE_SETTINGS -> closeSettingsOverlay();
        }
    }

    private void closeSettingsOverlay() {
        if (settingsOverlayController != null) {
            settingsOverlayController.closeSettings();
        }
    }

    private void openSettingsOverlay() {
        if (settingsOverlayController != null) {
            settingsOverlayController.openSettings();
        }
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 1 / 30f);

        if (contentHotReload != null) {
            contentHotReload.update(delta);
            if (contentHotReload.consumeReloadRequested()) {
                performHotReload();
            }
        }

        if (!paused) engine.update(delta);

        uiViewport.apply();
        stage.getBatch().setColor(Color.WHITE);
        if (Constants.DEBUG && debugOverlayView != null) {
            debugOverlayView.update();
        }
        stage.act(delta);
        stage.draw();
    }

    private void autosave(AutosaveEvent event) {
        if (player == null) return;
        services.getStateManager().saveQuests(QuestLog.MAPPER.get(player));
        services.getStateManager().saveDialogFlags(DialogFlags.MAPPER.get(player));
        services.getStateManager().setPlayerState(player);
        services.getStateManager().saveCounters(Counters.MAPPER.get(player));
        services.saveGame();

    }

    private void togglePause(PauseEvent event) {
        if (event == null) return;
        switch (event.action()) {
            case PAUSE -> setPaused(true);
            case RESUME -> {
                setPaused(false);
                services.getEventBus().fire(new UiOverlayEvent(UiOverlayEvent.Type.CLOSE_SETTINGS));
            }
            case TOGGLE -> setPaused(!paused);
        }
    }

    private void setPaused(boolean paused) {
        this.paused = paused;

        if (pauseView != null) {
            pauseView.setVisible(paused);
            if (paused) {
                pauseView.toFront();
                pauseView.resetSelection();
            }
        }

        if (gameUiGroup != null) {
            gameUiGroup.setVisible(!paused);
        }

        if (paused) {
            inputManager.setActiveState(UiControllerState.class);
        } else {
            inputManager.setActiveState(GameControllerState.class);
        }
    }

    private void performHotReload() {
        if (contentReloadService == null || contentHotReload == null) return;
        contentHotReload.beginReload();
        try {
            var playerPos = Transform.MAPPER.get(player).getPosition().cpy();
            float playerRot = Physic.MAPPER.get(player).getBody().getAngle();

            contentReloadService.reloadAll();

            MapAsset current = tiledManager.getCurrentMapAsset();
            tiledManager.setMap(tiledManager.loadMap(current));
            services.getEventBus().fire(new MapChangeEvent(current.name().toLowerCase()));

            Transform.MAPPER.get(player).getPosition().set(playerPos);
            Physic.MAPPER.get(player).getBody().setTransform(playerPos, playerRot);

            skin = services.getAssetManager().get(SkinAsset.DEFAULT);
            rebuildUiAfterHotReload();
        } finally {
            contentHotReload.endReload();
        }
    }

    private void rebuildUiAfterHotReload() {
        gameUiGroup.clearChildren();
        gameUiBuilder.buildGameUi(gameUiGroup, buildUiDependencies());

        if (settingsView != null) {
            settingsView.remove();
        }
        if (pauseView != null) {
            pauseView.remove();
        }
        if (debugOverlayView != null) {
            debugOverlayView.remove();
        }

        boolean showSettings = settingsViewModel.isOpen() && paused;
        GameUiBuilder.OverlayViews overlays = gameUiBuilder.buildOverlays(
            stage,
            buildUiDependencies(),
            paused,
            showSettings
        );
        pauseView = overlays.pauseView();
        settingsView = overlays.settingsView();
        debugOverlayView = overlays.debugOverlayView();
        settingsOverlayController = new SettingsOverlayController(
            stage,
            pauseView,
            settingsView,
            pauseView::selectSettings,
            settingsView::resetSelection
        );
    }

    private GameUiBuilder.UiDependencies buildUiDependencies() {
        return new GameUiBuilder.UiDependencies(
            stage,
            skin,
            gameViewModel,
            inventoryViewModel,
            chestViewModel,
            pauseViewModel,
            settingsViewModel,
            engine,
            services,
            Constants.DEBUG
        );
    }

    private void clearAllControllerCommands() {
        for (var e : engine.getEntitiesFor(com.badlogic.ashley.core.Family.all(Controller.class).get())) {
            Controller c = Controller.MAPPER.get(e);
            if (c == null) continue;
            c.getPressedCommands().clear();
            c.getReleasedCommands().clear();
            c.getHeldCommands().clear();
            c.getCommandBuffer().clear();
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
