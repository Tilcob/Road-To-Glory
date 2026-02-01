package com.github.tilcob.game.ui.overlay;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public final class SettingsOverlayController {
    private final Actor baseView;
    private final Actor settingsView;
    private final Runnable onBaseShown;
    private final Runnable onSettingsShown;

    public SettingsOverlayController(
        Actor baseView,
        Actor settingsView,
        Runnable onBaseShown,
        Runnable onSettingsShown
    ) {
        this.baseView = baseView;
        this.settingsView = settingsView;
        this.onBaseShown = onBaseShown;
        this.onSettingsShown = onSettingsShown;
    }

    public void openSettings() {
        if (baseView != null) {
            baseView.setVisible(false);
        }
        if (settingsView != null) {
            settingsView.setVisible(true);
            settingsView.toFront();
            if (onSettingsShown != null) {
                onSettingsShown.run();
            }
        }
    }

    public void closeSettings() {
        if (settingsView != null) {
            settingsView.setVisible(false);
        }
        if (baseView != null) {
            baseView.setVisible(true);
            baseView.toFront();
            if (onBaseShown != null) {
                onBaseShown.run();
            }
        }
    }
}
