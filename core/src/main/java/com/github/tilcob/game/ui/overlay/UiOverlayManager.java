package com.github.tilcob.game.ui.overlay;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.UiOverlayEvent;
import com.github.tilcob.game.input.GameControllerState;
import com.github.tilcob.game.input.InputManager;
import com.github.tilcob.game.input.UiControllerState;
import com.github.tilcob.game.ui.model.SettingsViewModel;
import com.github.tilcob.game.ui.view.PauseView;
import com.github.tilcob.game.ui.view.SettingsView;

public final class UiOverlayManager {
    private final GameEventBus eventBus;
    private final InputManager inputManager;
    private final Group gameUiGroup;
    private final WidgetGroup overlayGroup;
    private final PauseView pauseView;
    private final SettingsViewModel settingsViewModel;
    private final SettingsOverlayController settingsOverlayController;
    private final boolean closeSettingsOnShow;
    private boolean paused;

    public UiOverlayManager(
        Stage stage,
        GameEventBus eventBus,
        InputManager inputManager,
        Group gameUiGroup,
        PauseView pauseView,
        SettingsView settingsView,
        SettingsViewModel settingsViewModel,
        boolean closeSettingsOnShow
    ) {
        this.eventBus = eventBus;
        this.inputManager = inputManager;
        this.gameUiGroup = gameUiGroup;
        this.overlayGroup = new WidgetGroup();
        this.overlayGroup.setFillParent(true);
        this.pauseView = pauseView;
        this.settingsViewModel = settingsViewModel;
        this.settingsOverlayController = new SettingsOverlayController(
            pauseView,
            settingsView,
            pauseView::resetSelection,
            settingsView::resetSelection
        );
        this.closeSettingsOnShow = closeSettingsOnShow;

        if (stage != null) {
            stage.addActor(overlayGroup);
        }
        pauseView.remove();
        overlayGroup.addActor(pauseView);
        settingsView.remove();
        settingsView.setVisible(settingsViewModel != null && settingsViewModel.isOpen());
        overlayGroup.addActor(settingsView);
    }

    public void show() {
        eventBus.subscribe(UiOverlayEvent.class, this::onOverlayEvent);
        if (closeSettingsOnShow) {
            eventBus.fire(new UiOverlayEvent(UiOverlayEvent.Type.CLOSE_SETTINGS));
        }
    }

    public void hide() {
        eventBus.unsubscribe(UiOverlayEvent.class, this::onOverlayEvent);
        overlayGroup.remove();
    }

    public void setPaused(boolean paused) {
        boolean wasPaused = this.paused;
        this.paused = paused;

        pauseView.setVisible(paused);
        if (paused) {
            pauseView.toFront();
            pauseView.resetSelection();
            if (settingsViewModel != null && settingsViewModel.isOpen()) {
                openSettingsOverlay();
            } else {
                closeSettingsOverlay();
            }
        }

        if (gameUiGroup != null) {
            gameUiGroup.setVisible(!paused);
        }

        if (paused) {
            inputManager.setActiveState(UiControllerState.class);
        } else {
            closeSettingsOverlay();
            pauseView.setVisible(false);
            inputManager.setActiveState(GameControllerState.class);
            if (wasPaused) {
                eventBus.fire(new UiOverlayEvent(UiOverlayEvent.Type.CLOSE_SETTINGS));
            }
        }
    }

    public void dispose() {
        hide();
    }

    private void onOverlayEvent(UiOverlayEvent event) {
        if (event == null || !paused) return;

        switch (event.type()) {
            case OPEN_SETTINGS, TOGGLE_SETTINGS -> {
                boolean willOpen = (event.type() == UiOverlayEvent.Type.OPEN_SETTINGS)
                    || !settingsViewModel.isOpen();
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
        settingsOverlayController.closeSettings();
    }

    private void openSettingsOverlay() {
        settingsOverlayController.openSettings();
    }
}
