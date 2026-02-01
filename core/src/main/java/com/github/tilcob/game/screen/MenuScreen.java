package com.github.tilcob.game.screen;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.assets.MusicAsset;
import com.github.tilcob.game.assets.SkinAsset;
import com.github.tilcob.game.event.UiOverlayEvent;
import com.github.tilcob.game.input.IdleControllerState;
import com.github.tilcob.game.input.InputManager;
import com.github.tilcob.game.input.UiControllerState;
import com.github.tilcob.game.ui.model.MenuViewModel;
import com.github.tilcob.game.ui.model.SettingsViewModel;
import com.github.tilcob.game.ui.overlay.SettingsOverlayController;
import com.github.tilcob.game.ui.view.MenuView;
import com.github.tilcob.game.ui.view.SettingsView;

public class MenuScreen extends ScreenAdapter {
    private final GameServices services;
    private final Stage stage;
    private final Skin skin;
    private final Viewport uiViewport;
    private final InputManager inputManager;
    private final ScreenNavigator screenNavigator;
    private final IdleControllerState idleControllerState;
    private final UiControllerState uiControllerState;
    private MenuViewModel menuViewModel;
    private SettingsViewModel settingsViewModel;
    private MenuView menuView;
    private SettingsView settingsView;
    private SettingsOverlayController settingsOverlayController;

    public MenuScreen(
        GameServices services,
        Batch batch,
        InputManager inputManager,
        Viewport uiViewport,
        ScreenNavigator screenNavigator
    ) {
        this.services = services;
        this.uiViewport = uiViewport;
        this.stage = new Stage(uiViewport, batch);
        this.skin = services.getAssetManager().get(SkinAsset.DEFAULT);
        this.inputManager = inputManager;
        this.screenNavigator = screenNavigator;
        this.idleControllerState = new IdleControllerState();
        this.uiControllerState = new UiControllerState(services.getEventBus());

        services.getUiServices().setSkin(skin);
    }

    @Override
    public void resize(int width, int height) {
        uiViewport.update(width, height, true);
    }

    @Override
    public void show() {
        inputManager.setInputProcessors(stage);
        inputManager.configureStates(UiControllerState.class, idleControllerState, uiControllerState);

        menuViewModel = new MenuViewModel(services, screenNavigator);
        settingsViewModel = new SettingsViewModel(services);

        menuView = new MenuView(skin, stage, menuViewModel);
        settingsView = new SettingsView(skin, stage, settingsViewModel);

        settingsOverlayController = new SettingsOverlayController(
            stage,
            menuView,
            settingsView,
            menuView::selectSettings,
            settingsView::resetSelection
        );

        stage.addActor(menuView);

        services.getEventBus().subscribe(UiOverlayEvent.class, this::onOverlayEvent);
        services.getAudioManager().playMusic(MusicAsset.MENU);
        services.getEventBus().fire(new UiOverlayEvent(UiOverlayEvent.Type.CLOSE_SETTINGS));
    }

    @Override
    public void hide() {
        services.getEventBus().unsubscribe(UiOverlayEvent.class, this::onOverlayEvent);
        this.stage.clear();

        if (menuViewModel != null) {
            menuViewModel.dispose();
            menuViewModel = null;
        }
        if (settingsViewModel != null) {
            settingsViewModel.dispose();
            settingsViewModel = null;
        }

        menuView = null;
        settingsView = null;
        settingsOverlayController = null;
    }

    @Override
    public void render(float delta) {
        uiViewport.apply();
        stage.getBatch().setColor(Color.WHITE);
        stage.act(delta);
        stage.draw();
    }

    private void onOverlayEvent(UiOverlayEvent e) {
        if (e == null) return;
        switch (e.type()) {
            case OPEN_SETTINGS, TOGGLE_SETTINGS -> {
                boolean willOpen = (e.type() == UiOverlayEvent.Type.OPEN_SETTINGS) || !settingsViewModel.isOpen();
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
    public void dispose() {
        stage.dispose();
    }
}
