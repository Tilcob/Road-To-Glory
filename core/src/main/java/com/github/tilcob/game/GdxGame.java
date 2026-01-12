package com.github.tilcob.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.input.InputBindings;
import com.github.tilcob.game.input.InputBindingsStorage;
import com.github.tilcob.game.input.InputManager;
import com.github.tilcob.game.input.KeyboardInputDevice;
import com.github.tilcob.game.screen.LoadingScreen;
import com.github.tilcob.game.screen.MenuScreen;
import com.github.tilcob.game.screen.ScreenFactory;
import com.github.tilcob.game.screen.ScreenNavigator;

import java.util.HashMap;
import java.util.Map;

public class GdxGame extends Game implements ScreenNavigator {
    private Batch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private GLProfiler glProfiler;
    private FPSLogger fpsLogger;
    private InputMultiplexer inputMultiplexer;
    private InputManager inputManager;
    private final Map<Class<? extends Screen>, Screen> screenCache = new HashMap<>();
    private GameServices services;
    private ScreenFactory screenFactory;

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        inputMultiplexer = new InputMultiplexer();
        inputManager = new InputManager(inputMultiplexer);
        Gdx.input.setInputProcessor(inputMultiplexer);
        InputBindingsStorage bindingsStorage = new InputBindingsStorage("input/input_bindings.json",
            "input/input_bindings.json");
        InputBindings bindings = bindingsStorage.load();
        inputManager.addDevice(new KeyboardInputDevice(bindings));

        services = new GameServices(new InternalFileHandleResolver(),
            Gdx.files.local("savegame.json").path());
        services.loadGame();
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.WIDTH, Constants.HEIGHT, camera);
        glProfiler = new GLProfiler(Gdx.graphics);
        fpsLogger = new FPSLogger();
        screenFactory = new ScreenFactory(services, batch, camera, viewport, inputManager, this);

        glProfiler.enable();
        addScreen(screenFactory.createLoadingScreen(this::onLoadingFinished));
        setScreen(LoadingScreen.class);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        super.resize(width, height);
    }

    @Override
    public void render() {
        glProfiler.reset();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        super.render();

        Gdx.graphics.setTitle("GdxGame - Draw Cals: " + glProfiler.getDrawCalls()); // Draw calls should be minimized!!
        fpsLogger.log();
    }

    public void addScreen(Screen screen) {
        screenCache.put(screen.getClass(), screen);
    }

    public void removeScreen(Screen screen) {
        screenCache.remove(screen.getClass());
    }

    @Override
    public void setScreen(Class<? extends Screen> screenClass) {
        Screen screen = screenCache.get(screenClass);
        if (screen == null) {
            throw new GdxRuntimeException("No screen with class: " + screenClass + " found in the screen cache.");
        }
        super.setScreen(screen);
    }

    private void onLoadingFinished(LoadingScreen loadingScreen) {
        addScreen(screenFactory.createMenuScreen());
        addScreen(screenFactory.createGameScreen());
        removeScreen(loadingScreen);
        loadingScreen.dispose();
        setScreen(MenuScreen.class);
    }

    public Batch getBatch() {
        return batch;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public GameServices getServices() {
        return services;
    }

    public void setInputProcessors(InputProcessor... processors) {
        inputMultiplexer.clear();
        if (processors == null) return;

        for (InputProcessor processor : processors) {
            inputMultiplexer.addProcessor(processor);
        }
    }

    @Override
    public void dispose() {
        screenCache.values().forEach(Screen::dispose);
        screenCache.clear();
        batch.dispose();
        services.getAssetManager().debugDiagnostics();
        services.getAssetManager().dispose();

    }
}
