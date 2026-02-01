package com.github.tilcob.game.ui.overlay;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public final class SettingsOverlayController {
    private final Stage stage;
    private final Actor baseView;
    private final Actor settingsView;
    private final Runnable onBaseShown;
    private final Runnable onSettingsShown;

    public SettingsOverlayController(
        Stage stage,
        Actor baseView,
        Actor settingsView,
        Runnable onBaseShown,
        Runnable onSettingsShown
    ) {
        this.stage = stage;
        this.baseView = baseView;
        this.settingsView = settingsView;
        this.onBaseShown = onBaseShown;
        this.onSettingsShown = onSettingsShown;
    }

    public void openSettings() {
        if (baseView != null && baseView.getStage() != null) {
            baseView.remove();
        }
        if (settingsView != null) {
            settingsView.setVisible(true);
            if (settingsView.getStage() == null && stage != null) {
                stage.addActor(settingsView);
            }
            settingsView.toFront();
            if (onSettingsShown != null) {
                onSettingsShown.run();
            }
        }
    }

    public void closeSettings() {
        if (settingsView != null && settingsView.getStage() != null) {
            settingsView.remove();
        }
        if (baseView != null) {
            if (baseView.getStage() == null && stage != null) {
                stage.addActor(baseView);
            }
            baseView.toFront();
            if (onBaseShown != null) {
                onBaseShown.run();
            }
        }
    }
}
