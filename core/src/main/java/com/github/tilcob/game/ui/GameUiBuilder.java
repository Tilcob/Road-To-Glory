package com.github.tilcob.game.ui;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.ui.model.ChestViewModel;
import com.github.tilcob.game.ui.model.GameViewModel;
import com.github.tilcob.game.ui.model.InventoryViewModel;
import com.github.tilcob.game.ui.model.PauseViewModel;
import com.github.tilcob.game.ui.model.SettingsViewModel;
import com.github.tilcob.game.ui.view.ChestView;
import com.github.tilcob.game.ui.view.DebugOverlayView;
import com.github.tilcob.game.ui.view.GameView;
import com.github.tilcob.game.ui.view.InventoryView;
import com.github.tilcob.game.ui.view.PauseView;
import com.github.tilcob.game.ui.view.SettingsView;

public class GameUiBuilder {
    public record UiDependencies(
        Stage stage,
        Skin skin,
        GameViewModel gameViewModel,
        InventoryViewModel inventoryViewModel,
        ChestViewModel chestViewModel,
        PauseViewModel pauseViewModel,
        SettingsViewModel settingsViewModel,
        Engine engine,
        GameServices services,
        boolean debugEnabled
    ) {
    }

    public record OverlayViews(
        PauseView pauseView,
        SettingsView settingsView,
        DebugOverlayView debugOverlayView
    ) {
    }

    public void buildGameUi(Group gameUiGroup, UiDependencies dependencies) {
        GameView gameView = new GameView(
            dependencies.skin(),
            dependencies.stage(),
            dependencies.gameViewModel()
        );
        InventoryView inventoryView = new InventoryView(
            dependencies.skin(),
            dependencies.stage(),
            dependencies.inventoryViewModel()
        );
        ChestView chestView = new ChestView(
            dependencies.skin(),
            dependencies.stage(),
            dependencies.chestViewModel()
        );

        gameUiGroup.addActor(gameView);
        gameUiGroup.addActor(inventoryView);
        gameUiGroup.addActor(chestView);
    }

    public OverlayViews buildOverlays(Stage stage, UiDependencies dependencies, boolean paused, boolean showSettings) {
        SettingsView settingsView = new SettingsView(
            dependencies.skin(),
            stage,
            dependencies.settingsViewModel()
        );
        PauseView pauseView = new PauseView(
            dependencies.skin(),
            stage,
            dependencies.pauseViewModel()
        );
        DebugOverlayView debugOverlayView = null;

        if (showSettings) {
            stage.addActor(settingsView);
        }

        pauseView.setVisible(paused);
        stage.addActor(pauseView);

        if (dependencies.debugEnabled()) {
            debugOverlayView = new DebugOverlayView(
                dependencies.skin(),
                dependencies.engine(),
                dependencies.services()
            );
            stage.addActor(debugOverlayView);
        }

        return new OverlayViews(pauseView, settingsView, debugOverlayView);
    }
}
