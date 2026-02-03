package com.github.tilcob.game;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.github.tilcob.game.assets.AssetManager;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.component.Counters;
import com.github.tilcob.game.component.DialogFlags;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.cutscene.CutsceneData;
import com.github.tilcob.game.cutscene.CutsceneRepository;
import com.github.tilcob.game.dialog.DialogData;
import com.github.tilcob.game.dialog.DialogRepository;
import com.github.tilcob.game.entity.EntityLookup;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.flow.FlowBootstrap;
import com.github.tilcob.game.input.ActiveEntityReference;
import com.github.tilcob.game.inventory.InventoryService;
import com.github.tilcob.game.item.ItemEntityRegistry;
import com.github.tilcob.game.quest.*;
import com.github.tilcob.game.save.SaveManager;
import com.github.tilcob.game.save.SaveService;
import com.github.tilcob.game.save.SaveSlot;
import com.github.tilcob.game.save.registry.ChestRegistry;
import com.github.tilcob.game.save.states.StateManager;
import com.github.tilcob.game.ui.UiServices;
import com.github.tilcob.game.yarn.CutsceneYarnRuntime;
import com.github.tilcob.game.yarn.DialogYarnRuntime;
import com.github.tilcob.game.yarn.QuestYarnRuntime;

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
    private ActiveEntityReference activeEntityReference;
    private EntityLookup entityLookup;
    private UiServices uiServices;

    public GameServices(InternalFileHandleResolver resolver, String savePath) {
        init(resolver, new SaveManager(savePath));
    }

    public GameServices(InternalFileHandleResolver resolver, String saveDirectory, SaveSlot saveSlot) {
        init(resolver, new SaveManager(saveDirectory, saveSlot));
    }

    private void init(InternalFileHandleResolver resolver, SaveManager saveManager) {
        GameServicesBuilder.Components components = new GameServicesBuilder(
            resolver,
            saveManager,
            this::getEntityLookup
        ).build();
        this.eventBus = components.eventBus();
        this.itemEntityRegistry = components.itemEntityRegistry();
        this.chestRegistry = components.chestRegistry();
        this.stateManager = components.stateManager();
        this.saveService = components.saveService();
        this.assetManager = components.assetManager();
        this.audioManager = components.audioManager();
        this.allQuests = components.allQuests();
        this.allQuestDialogs = components.allQuestDialogs();
        this.allDialogs = components.allDialogs();
        this.allCutscenes = components.allCutscenes();
        this.questYarnRegistry = components.questYarnRegistry();
        this.dialogRepository = components.dialogRepository();
        this.cutsceneRepository = components.cutsceneRepository();
        this.dialogYarnRuntime = components.dialogYarnRuntime();
        this.cutsceneYarnRuntime = components.cutsceneYarnRuntime();
        this.questYarnRuntime = components.questYarnRuntime();
        this.questManager = components.questManager();
        this.questLifecycleService = components.questLifecycleService();
        this.questRewardService = components.questRewardService();
        this.inventoryService = components.inventoryService();
        this.flowBootstrap = components.flowBootstrap();
        this.uiServices = components.uiServices();
    }

    public void loadGame() {
        saveService.loadGame();
    }

    public void saveGame() {
        Entity player = null;
        if (activeEntityReference != null) {
            player = activeEntityReference.get();
        }
        if (player == null && entityLookup != null) {
            player = entityLookup.getPlayer();
        }
        if (player != null) {
            stateManager.saveQuests(QuestLog.MAPPER.get(player));
            stateManager.saveDialogFlags(DialogFlags.MAPPER.get(player));
            stateManager.setPlayerState(player);
            stateManager.saveCounters(Counters.MAPPER.get(player));
        }
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

    public UiServices getUiServices() {
        return uiServices;
    }

    public ActiveEntityReference getActiveEntityReference() {
        return activeEntityReference;
    }

    public void setActiveEntityReference(ActiveEntityReference activeEntityReference) {
        this.activeEntityReference = activeEntityReference;
    }
}
