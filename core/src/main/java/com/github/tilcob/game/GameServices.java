package com.github.tilcob.game;

import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.github.tilcob.game.assets.AssetManager;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.cutscene.CutsceneData;
import com.github.tilcob.game.cutscene.CutsceneRepository;
import com.github.tilcob.game.dialog.DialogData;
import com.github.tilcob.game.dialog.DialogRepository;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.inventory.InventoryService;
import com.github.tilcob.game.item.ItemEntityRegistry;
import com.github.tilcob.game.quest.*;
import com.github.tilcob.game.save.SaveManager;
import com.github.tilcob.game.save.SaveService;
import com.github.tilcob.game.save.SaveSlot;
import com.github.tilcob.game.save.registry.ChestRegistry;
import com.github.tilcob.game.save.states.GameState;
import com.github.tilcob.game.save.states.StateManager;
import com.github.tilcob.game.yarn.*;

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
    private final Map<String, CutsceneData> allCutscenes;
    private final QuestYarnRegistry questYarnRegistry;
    private final DialogRepository dialogRepository;
    private final CutsceneRepository cutsceneRepository;
    private final DialogYarnRuntime dialogYarnRuntime;
    private final CutsceneYarnRuntime cutsceneYarnRuntime;
    private final QuestYarnRuntime questYarnRuntime;
    private final QuestManager questManager;
    private final QuestLifecycleService questLifecycleService;
    private final QuestRewardService questRewardService;
    private final InventoryService inventoryService;
    private EntityLookup entityLookup;

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
        this.allCutscenes = new HashMap<>();
        this.questYarnRegistry = new QuestYarnRegistry("quests/index.json");
        this.questLifecycleService = new QuestLifecycleService(eventBus, questYarnRegistry, allDialogs);
        this.questRewardService = new QuestRewardService(eventBus, questYarnRegistry);
        this.dialogRepository = new DialogRepository(true, "dialogs",
            Map.of("Shopkeeper", "shopkeeper"));
        this.cutsceneRepository = new CutsceneRepository(true, "cutscenes");
        DialogYarnBridge dialogYarnBridge = new DialogYarnBridge();
        CutsceneYarnBridge cutsceneYarnBridge = new CutsceneYarnBridge(audioManager, eventBus, this::getEntityLookup);
        QuestYarnBridge questYarnBridge = new QuestYarnBridge(questLifecycleService);
        this.dialogYarnRuntime = new DialogYarnRuntime(dialogYarnBridge);
        this.cutsceneYarnRuntime = new CutsceneYarnRuntime(cutsceneYarnBridge);
        this.questYarnRuntime = new QuestYarnRuntime(questYarnBridge, allDialogs, allQuestDialogs);
        this.questLifecycleService.setQuestYarnRuntime(questYarnRuntime);
        this.questManager = new QuestManager(questYarnRuntime);
        this.inventoryService = new InventoryService(eventBus);
    }

    public GameServices(InternalFileHandleResolver resolver, String saveDirectory, SaveSlot saveSlot) {
        this.eventBus = new GameEventBus();
        this.itemEntityRegistry = new ItemEntityRegistry(eventBus);
        this.chestRegistry = new ChestRegistry();
        this.stateManager = new StateManager(new GameState());
        SaveManager saveManager = new SaveManager(saveDirectory, saveSlot);
        this.saveService = new SaveService(saveManager, stateManager, chestRegistry);
        this.assetManager = new AssetManager(resolver);
        this.audioManager = new AudioManager(assetManager);
        this.allQuests = new HashMap<>();
        this.allQuestDialogs = new HashMap<>();
        this.allDialogs = new HashMap<>();
        this.allCutscenes = new HashMap<>();
        this.questYarnRegistry = new QuestYarnRegistry("quests/index.json");
        this.questLifecycleService = new QuestLifecycleService(eventBus, questYarnRegistry, allDialogs);
        this.questRewardService = new QuestRewardService(eventBus, questYarnRegistry);
        this.dialogRepository = new DialogRepository(true, "dialogs",
            Map.of("Shopkeeper", "shopkeeper"));
        this.cutsceneRepository = new CutsceneRepository(true, "cutscenes");
        DialogYarnBridge dialogYarnBridge = new DialogYarnBridge();
        CutsceneYarnBridge cutsceneYarnBridge = new CutsceneYarnBridge(audioManager, eventBus, this::getEntityLookup);
        QuestYarnBridge questYarnBridge = new QuestYarnBridge(questLifecycleService);
        this.dialogYarnRuntime = new DialogYarnRuntime(dialogYarnBridge);
        this.cutsceneYarnRuntime = new CutsceneYarnRuntime(cutsceneYarnBridge);
        this.questYarnRuntime = new QuestYarnRuntime(questYarnBridge, allDialogs, allQuestDialogs);
        this.questLifecycleService.setQuestYarnRuntime(questYarnRuntime);
        this.questManager = new QuestManager(questYarnRuntime);
        this.inventoryService = new InventoryService(eventBus);
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

    public Map<String, CutsceneData> getAllCutscenes() {
        return allCutscenes;
    }

    public QuestYarnRegistry getQuestYarnRegistry() {
        return questYarnRegistry;
    }

    public DialogRepository getDialogRepository() {
        return dialogRepository;
    }

    public CutsceneRepository getCutsceneRepository() {
        return cutsceneRepository;
    }

    public DialogYarnRuntime getDialogYarnRuntime() {
        return dialogYarnRuntime;
    }

    public CutsceneYarnRuntime getCutsceneYarnRuntime() {
        return cutsceneYarnRuntime;
    }

    public QuestYarnRuntime getQuestYarnRuntime() {
        return questYarnRuntime;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public QuestLifecycleService getQuestLifecycleService() {
        return questLifecycleService;
    }

    public QuestRewardService getQuestRewardService() {
        return questRewardService;
    }

    public InventoryService getInventoryService() {
        return inventoryService;
    }

    public EntityLookup getEntityLookup() {
        return entityLookup;
    }

    public void setEntityLookup(EntityLookup entityLookup) {
        this.entityLookup = entityLookup;
    }
}
