package com.github.tilcob.game;

import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.github.tilcob.game.assets.AssetManager;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.cutscene.CutsceneData;
import com.github.tilcob.game.cutscene.CutsceneRepository;
import com.github.tilcob.game.dialog.DialogData;
import com.github.tilcob.game.dialog.DialogRepository;
import com.github.tilcob.game.entity.EntityLookup;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.flow.FlowBootstrap;
import com.github.tilcob.game.inventory.InventoryService;
import com.github.tilcob.game.item.ItemEntityRegistry;
import com.github.tilcob.game.quest.*;
import com.github.tilcob.game.save.SaveManager;
import com.github.tilcob.game.save.SaveService;
import com.github.tilcob.game.save.SaveSlot;
import com.github.tilcob.game.save.registry.ChestRegistry;
import com.github.tilcob.game.save.states.GameState;
import com.github.tilcob.game.save.states.StateManager;
import com.github.tilcob.game.yarn.CutsceneYarnRuntime;
import com.github.tilcob.game.yarn.DialogYarnRuntime;
import com.github.tilcob.game.yarn.QuestYarnRuntime;
import com.github.tilcob.game.yarn.YarnRuntime;

import java.util.HashMap;
import java.util.Map;

public class GameServices {
    private GameEventBus eventBus;
    private ItemEntityRegistry itemEntityRegistry;
    private ChestRegistry chestRegistry;
    private StateManager stateManager;
    private SaveService saveService;
    private AssetManager assetManager;
    private AudioManager audioManager;
    private Map<String, Quest> allQuests;
    private Map<String, DialogData> allQuestDialogs;
    private Map<String, DialogData> allDialogs;
    private Map<String, CutsceneData> allCutscenes;
    private QuestYarnRegistry questYarnRegistry;
    private DialogRepository dialogRepository;
    private CutsceneRepository cutsceneRepository;
    private DialogYarnRuntime dialogYarnRuntime;
    private CutsceneYarnRuntime cutsceneYarnRuntime;
    private QuestYarnRuntime questYarnRuntime;
    private QuestManager questManager;
    private QuestLifecycleService questLifecycleService;
    private QuestRewardService questRewardService;
    private InventoryService inventoryService;
    private FlowBootstrap flowBootstrap;
    private EntityLookup entityLookup;

    public GameServices(InternalFileHandleResolver resolver, String savePath) {
        init(resolver, new SaveManager(savePath));
    }

    public GameServices(InternalFileHandleResolver resolver, String saveDirectory, SaveSlot saveSlot) {
        init(resolver, new SaveManager(saveDirectory, saveSlot));
    }

    private void init(InternalFileHandleResolver resolver, SaveManager saveManager) {
        this.eventBus = new GameEventBus();
        this.itemEntityRegistry = new ItemEntityRegistry(eventBus);
        this.chestRegistry = new ChestRegistry();
        this.stateManager = new StateManager(new GameState());
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
        this.flowBootstrap = FlowBootstrap.create(eventBus, questLifecycleService, audioManager, this::getEntityLookup);
        YarnRuntime runtime = new YarnRuntime();
        this.dialogYarnRuntime = new DialogYarnRuntime(runtime, flowBootstrap.commands(), flowBootstrap.executor(), flowBootstrap.functions());
        this.cutsceneYarnRuntime = new CutsceneYarnRuntime(runtime, flowBootstrap.commands(), flowBootstrap.executor());
        this.questYarnRuntime = new QuestYarnRuntime(
            runtime, questYarnRegistry, flowBootstrap.commands(),
            flowBootstrap.executor(), flowBootstrap.functions());
        this.questLifecycleService.setQuestYarnRuntime(questYarnRuntime);
        this.questManager = new QuestManager(questYarnRuntime);
        this.inventoryService = new InventoryService(eventBus);

        questLifecycleService.setQuestManager(questManager);
        questLifecycleService.setInventoryService(inventoryService);
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

    public ItemEntityRegistry getItemEntityRegistry() {
        return itemEntityRegistry;
    }

    public FlowBootstrap getFlowBootstrap() {
        return flowBootstrap;
    }

    public void setEntityLookup(EntityLookup entityLookup) {
        this.entityLookup = entityLookup;
    }
}
