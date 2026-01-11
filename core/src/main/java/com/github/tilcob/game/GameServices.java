package com.github.tilcob.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.github.tilcob.game.assets.AssetManager;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.dialog.MapDialogData;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.item.ItemRegistry;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.save.SaveManager;
import com.github.tilcob.game.save.registry.ChestRegistry;
import com.github.tilcob.game.save.states.GameState;
import com.github.tilcob.game.save.states.StateManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GameServices {
    private final GameEventBus eventBus;
    private final ItemRegistry itemRegistry;
    private final ChestRegistry chestRegistry;
    private final StateManager stateManager;
    private final SaveManager saveManager;
    private final AssetManager assetManager;
    private final AudioManager audioManager;
    private final Map<String, Quest> allQuests;
    private final Map<String, MapDialogData> allDialogs;

    public GameServices(InternalFileHandleResolver resolver, String savePath) {
        this.eventBus = new GameEventBus();
        this.itemRegistry = new ItemRegistry(eventBus);
        this.chestRegistry = new ChestRegistry();
        this.stateManager = new StateManager(new GameState());
        this.saveManager = new SaveManager(savePath);
        this.assetManager = new AssetManager(resolver);
        this.audioManager = new AudioManager(assetManager);
        this.allQuests = new HashMap<>();
        this.allDialogs = new HashMap<>();
    }

    public void loadGame() {
        if (saveManager.exists()) {
            try {
                GameState loaded = saveManager.load();
                stateManager.setGameState(loaded);
                chestRegistry.loadFromState(stateManager.loadChestRegistryState());
            } catch (IOException e) {
                Gdx.app.error("GameServices", "Error loading state: " + e.getMessage());
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
            Gdx.app.error("GameServices", "Error saving state: " + e.getMessage());
        }
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

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public Map<String, Quest> getAllQuests() {
        return allQuests;
    }

    public Map<String, MapDialogData> getAllDialogs() {
        return allDialogs;
    }
}
