package com.github.tilcob.game.screen;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.input.InputManager;

import java.util.function.Consumer;

public class ScreenFactory {
    private final GameServices services;
    private final Batch batch;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final InputManager inputManager;
    private final ScreenNavigator screenNavigator;

    public ScreenFactory(
        GameServices services,
        Batch batch,
        OrthographicCamera camera,
        Viewport viewport,
        InputManager inputManager,
        ScreenNavigator screenNavigator
    ) {
        this.services = services;
        this.batch = batch;
        this.camera = camera;
        this.viewport = viewport;
        this.inputManager = inputManager;
        this.screenNavigator = screenNavigator;
    }

    public LoadingScreen createLoadingScreen(Consumer<LoadingScreen> onFinished) {
        return new LoadingScreen(services, onFinished);
    }

    public MenuScreen createMenuScreen() {
        Viewport uiViewport = new FitViewport(800f, 450f);
        return new MenuScreen(services, batch, inputManager, uiViewport, screenNavigator);
    }

    public GameScreen createGameScreen() {
        Viewport uiViewport = new FitViewport(640f, 360f);
        GameScreenModule module = new GameScreenModule(services, batch, camera, viewport, inputManager, screenNavigator);
        return module.create(uiViewport);
    }
}
