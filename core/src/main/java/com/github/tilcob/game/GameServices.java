package com.github.tilcob.game;

import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.github.tilcob.game.assets.AssetManager;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.dialog.DialogData;
import com.github.tilcob.game.dialog.DialogRepository;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.item.ItemEntityRegistry;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.QuestManager;
import com.github.tilcob.game.quest.QuestYarnRegistry;
import com.github.tilcob.game.save.SaveManager;
import com.github.tilcob.game.save.SaveService;
import com.github.tilcob.game.save.registry.ChestRegistry;
import com.github.tilcob.game.save.states.GameState;
import com.github.tilcob.game.save.states.StateManager;
import com.github.tilcob.game.yarn.DialogYarnRuntime;
import com.github.tilcob.game.yarn.QuestYarnBridge;
import com.github.tilcob.game.yarn.QuestYarnRuntime;

import java.util.HashMap;
import java.util.Map;

public class GameServices {
    private final GameEventBus eventBus;
    private final ItemEntityRegistry itemEntityRegistry;
    private final ChestRegistry chestRegistry;
    private final StateManager stateManager;
    private final SaveService saveService;
    private final AssetManager assetManager;
    private final AudioManager audioManager;
    private final Map<String, Quest> allQuests;
    private final Map<String, DialogData> allQuestDialogs;
    private final Map<String, DialogData> allDialogs;
    private final QuestYarnRegistry questYarnRegistry;
    private final DialogRepository dialogRepository;
    private final QuestYarnBridge questYarnBridge;
    private final DialogYarnRuntime dialogYarnRuntime;
    private final QuestYarnRuntime questYarnRuntime;
    private final QuestManager questManager;

    public GameServices(InternalFileHandleResolver resolver, String savePath) {
        this.eventBus = new GameEventBus();
        this.itemEntityRegistry = new ItemEntityRegistry(eventBus);
        this.chestRegistry = new ChestRegistry();
        this.stateManager = new StateManager(new GameState());
        SaveManager saveManager = new SaveManager(savePath);
        this.saveService = new SaveService(saveManager, stateManager, chestRegistry);
        this.assetManager = new AssetManager(resolver);
        this.audioManager = new AudioManager(assetManager);
        this.allQuests = new HashMap<>();
        this.allQuestDialogs = new HashMap<>();
        this.allDialogs = new HashMap<>();
        this.questYarnRegistry = new QuestYarnRegistry("quests/index.json");
        this.dialogRepository = new DialogRepository(true, "dialogs",
            Map.of("Shopkeeper", "shopkeeper"));
        QuestYarnBridge dialogYarnBridge = new QuestYarnBridge(eventBus, true);
        this.questYarnBridge = new QuestYarnBridge(eventBus, false);
        this.dialogYarnRuntime = new DialogYarnRuntime(dialogYarnBridge);
        this.questYarnRuntime = new QuestYarnRuntime(questYarnBridge);
        this.questManager = new QuestManager(questYarnRuntime, allDialogs, allQuestDialogs);
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

    public ItemEntityRegistry getItemRegistry() {
        return itemEntityRegistry;
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

    public Map<String, DialogData> getAllQuestDialogs() {
        return allQuestDialogs;
    }

    public Map<String, DialogData> getAllDialogs() {
        return allDialogs;
    }

    public QuestYarnRegistry getQuestYarnRegistry() {
        return questYarnRegistry;
    }

    public DialogRepository getDialogRepository() {
        return dialogRepository;
    }

    public DialogYarnRuntime getDialogYarnRuntime() {
        return dialogYarnRuntime;
    }

    public QuestYarnRuntime getQuestYarnRuntime() {
        return questYarnRuntime;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }
}
