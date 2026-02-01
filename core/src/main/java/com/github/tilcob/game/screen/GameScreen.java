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
import com.github.tilcob.game.ui.GameUiBuilder;
import com.github.tilcob.game.ui.model.*;
import com.github.tilcob.game.ui.overlay.UiOverlayManager;
import com.github.tilcob.game.ui.view.DebugOverlayView;
import com.github.tilcob.game.ui.view.PauseView;
import com.github.tilcob.game.ui.view.SettingsView;
import com.github.tilcob.game.world.GameWorldLoader;

import java.util.function.Consumer;

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
    private ActiveEntityReference activeEntityReference;
    private PauseView pauseView;
    private SettingsView settingsView;
    private DebugOverlayView debugOverlayView;
    private boolean paused;
    private ContentReloadService contentReloadService;
    private ContentHotReload contentHotReload;
    private UiOverlayManager uiOverlayManager;
    private GameWorldLoader worldLoader;

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
        this.worldLoader = new GameWorldLoader(new GameWorldLoader.Dependencies(
                services,
                engine,
                tiledManager,
                tiledAshleyConfigurator,
                audioManager,
                physicWorld,
                uiViewport,
                activeEntityReference));

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
        inputManager.configureStates(GameControllerState.class, idleControllerState, gameControllerState,
                uiControllerState);
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
                false);
        pauseView = overlays.pauseView();
        settingsView = overlays.settingsView();
        debugOverlayView = overlays.debugOverlayView();
        uiOverlayManager = new UiOverlayManager(
                stage,
                services.getEventBus(),
                inputManager,
                gameUiGroup,
                pauseView,
                settingsView,
                settingsViewModel,
                true);
        uiOverlayManager.show();
        uiOverlayManager.setPaused(false);

        worldLoader.initializeWorld();
        worldLoader.loadState();
    }

    @Override
    public void hide() {
        engine.removeAllEntities();
        stage.clear();
        if (uiOverlayManager != null) {
            uiOverlayManager.hide();
        }

        if (settingsViewModel != null) {
            settingsViewModel.dispose();
            settingsViewModel = null;
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
        Entity player = getPlayer();
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
            case RESUME -> setPaused(false);
            case TOGGLE -> setPaused(!paused);
        }
    }

    private void setPaused(boolean paused) {
        this.paused = paused;

        if (uiOverlayManager != null) {
            uiOverlayManager.setPaused(paused);
        }
    }

    private void performHotReload() {
        if (contentReloadService == null || contentHotReload == null) return;
        Entity player = getPlayer();
        if (player == null) return;
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
                showSettings);
        pauseView = overlays.pauseView();
        settingsView = overlays.settingsView();
        debugOverlayView = overlays.debugOverlayView();
        if (uiOverlayManager != null)
            uiOverlayManager.hide();
        uiOverlayManager = new UiOverlayManager(
                stage,
                settingsViewModel.getEventBus(),
                inputManager,
                gameUiGroup,
                pauseView,
                settingsView,
                settingsViewModel,
                false);
        uiOverlayManager.show();
        uiOverlayManager.setPaused(paused);
    }

    private Entity getPlayer() {
        if (worldLoader == null) return null;
        return worldLoader.getPlayer();
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
                Constants.DEBUG);
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
        if (audioManager != null) audioManager.dispose();
        if (uiOverlayManager != null) {
            uiOverlayManager.dispose();
        }
        gameViewModel.dispose();
        pauseViewModel.dispose();
        physicWorld.dispose();
        stage.dispose();
    }
}
