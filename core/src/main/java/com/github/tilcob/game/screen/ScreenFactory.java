package com.github.tilcob.game.screen;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tilcob.game.GameServices;

import java.util.function.Consumer;

public class ScreenFactory {
    private final GameServices services;
    private final Batch batch;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final InputMultiplexer inputMultiplexer;
    private final ScreenNavigator screenNavigator;

    public ScreenFactory(
        GameServices services,
        Batch batch,
        OrthographicCamera camera,
        Viewport viewport,
        InputMultiplexer inputMultiplexer,
        ScreenNavigator screenNavigator
    ) {
        this.services = services;
        this.batch = batch;
        this.camera = camera;
        this.viewport = viewport;
        this.inputMultiplexer = inputMultiplexer;
        this.screenNavigator = screenNavigator;
    }

    public LoadingScreen createLoadingScreen(Consumer<LoadingScreen> onFinished) {
        return new LoadingScreen(services, onFinished);
    }

    public MenuScreen createMenuScreen() {
        Viewport uiViewport = new FitViewport(800f, 450f);
        return new MenuScreen(services, batch, inputMultiplexer, uiViewport, screenNavigator);
    }

    public GameScreen createGameScreen() {
        Viewport uiViewport = new FitViewport(640f, 360f);
        GameScreenModule module = new GameScreenModule(services, batch, camera, viewport, inputMultiplexer, screenNavigator);
        return module.create(uiViewport);
    }
}
