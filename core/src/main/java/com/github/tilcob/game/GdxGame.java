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
import com.github.tilcob.game.assets.AssetManager;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.item.ItemRegistry;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.QuestFactory;
import com.github.tilcob.game.quest.QuestLoader;
import com.github.tilcob.game.save.SaveManager;
import com.github.tilcob.game.save.registry.ChestRegistry;
import com.github.tilcob.game.save.states.GameState;
import com.github.tilcob.game.save.states.StateManager;
import com.github.tilcob.game.screen.LoadingScreen;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GdxGame extends Game {
    private Batch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private AssetManager assetManager;
    private AudioManager audioManager;
    private GLProfiler glProfiler;
    private FPSLogger fpsLogger;
    private InputMultiplexer inputMultiplexer;
    private final GameEventBus eventBus = new GameEventBus();
    private final ItemRegistry itemRegistry = new ItemRegistry(eventBus);
    private final ChestRegistry chestRegistry = new ChestRegistry();
    private final StateManager stateManager = new StateManager(new GameState());
    private SaveManager saveManager;
    private final Map<Class<? extends Screen>, Screen> screenCache = new HashMap<>();
    private final Map<String, Quest> allQuests = new HashMap<>();

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        inputMultiplexer = new InputMultiplexer();
        Gdx.input.setInputProcessor(inputMultiplexer);
        saveManager = new SaveManager(
            Gdx.files.local("savegame.json").path()
        );
        loadGame();
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.WIDTH, Constants.HEIGHT, camera);
        assetManager = new AssetManager(new InternalFileHandleResolver());
        audioManager = new AudioManager(assetManager);
        glProfiler = new GLProfiler(Gdx.graphics);
        fpsLogger = new FPSLogger();

        glProfiler.enable();
        addScreen(new LoadingScreen(this, assetManager));
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

    public void setScreen(Class<? extends Screen> screenClass) {
        Screen screen = screenCache.get(screenClass);
        if (screen == null) {
            throw new GdxRuntimeException("No screen with class: " + screenClass + " found in the screen cache.");
        }
        super.setScreen(screen);
    }

    public void loadGame() {
        if (saveManager.exists()) {
            try {
                GameState loaded = saveManager.load();
                stateManager.setGameState(loaded);
                chestRegistry.loadFromState(stateManager.loadChestRegistryState());
            } catch (IOException e) {
                Gdx.app.error("GdxGame", "Error loading state: " + e.getMessage());
            }
        }  else {
            stateManager.setGameState(new GameState());
        }
    }

    public void saveGame() {
        try {
            stateManager.saveChestRegistryState(chestRegistry.toState());
            saveManager.save(stateManager.getGameState());
        } catch (IOException e) {
            Gdx.app.error("GdxGame", "Error saving state: " + e.getMessage());
        }
    }

    @Override
    public void dispose() {
        screenCache.values().forEach(Screen::dispose);
        screenCache.clear();
        batch.dispose();
        assetManager.debugDiagnostics();
        assetManager.dispose();
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

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public GameEventBus getEventBus() {
        return eventBus;
    }

    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    public ChestRegistry getChestRegistry() {
        return chestRegistry;
    }

    public StateManager getStateManager() {
        return stateManager;
    }

    public SaveManager getSaveManager() {
        return saveManager;
    }

    public Map<String, Quest> getAllQuests() {
        return allQuests;
    }

    public void setInputProcessors(InputProcessor... processors) {
        inputMultiplexer.clear();
        if (processors == null) return;

        for (InputProcessor processor : processors) {
            inputMultiplexer.addProcessor(processor);
        }
    }
}
