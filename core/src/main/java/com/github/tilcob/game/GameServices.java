package com.github.tilcob.game;

import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.github.tilcob.game.assets.AssetManager;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.dialog.DialogData;
import com.github.tilcob.game.dialog.MapDialogData;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.item.ItemRegistry;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.save.SaveManager;
import com.github.tilcob.game.save.SaveService;
import com.github.tilcob.game.save.registry.ChestRegistry;
import com.github.tilcob.game.save.states.GameState;
import com.github.tilcob.game.save.states.StateManager;

import java.util.HashMap;
import java.util.Map;

public class GameServices {
    private final GameEventBus eventBus;
    private final ItemRegistry itemRegistry;
    private final ChestRegistry chestRegistry;
    private final StateManager stateManager;
    private final SaveService saveService;
    private final AssetManager assetManager;
    private final AudioManager audioManager;
    private final Map<String, Quest> allQuests;
    private final Map<String, DialogData> allDialogs;

    public GameServices(InternalFileHandleResolver resolver, String savePath) {
        this.eventBus = new GameEventBus();
        this.itemRegistry = new ItemRegistry(eventBus);
        this.chestRegistry = new ChestRegistry();
        this.stateManager = new StateManager(new GameState());
        SaveManager saveManager = new SaveManager(savePath);
        this.saveService = new SaveService(saveManager, stateManager, chestRegistry);
        this.assetManager = new AssetManager(resolver);
        this.audioManager = new AudioManager(assetManager);
        this.allQuests = new HashMap<>();
        this.allDialogs = new HashMap<>();
    }

    public void loadGame() {
        saveService.loadGame();
    }

    public void saveGame() {
        saveService.saveGame();
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

    public SaveService getSaveService() {
        return saveService;
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

    public Map<String, DialogData> getAllDialogs() {
        return allDialogs;
    }
}
