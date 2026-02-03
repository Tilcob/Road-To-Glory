package com.github.tilcob.game;

import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.github.tilcob.game.assets.AssetManager;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.audio.AudioModule;
import com.github.tilcob.game.cutscene.CutsceneData;
import com.github.tilcob.game.cutscene.CutsceneModule;
import com.github.tilcob.game.cutscene.CutsceneRepository;
import com.github.tilcob.game.dialog.DialogData;
import com.github.tilcob.game.dialog.DialogModule;
import com.github.tilcob.game.dialog.DialogRepository;
import com.github.tilcob.game.entity.EntityLookup;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.flow.FlowBootstrap;
import com.github.tilcob.game.inventory.InventoryService;
import com.github.tilcob.game.item.ItemEntityRegistry;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.QuestLifecycleService;
import com.github.tilcob.game.quest.QuestManager;
import com.github.tilcob.game.quest.QuestModule;
import com.github.tilcob.game.quest.QuestRewardService;
import com.github.tilcob.game.quest.QuestYarnRegistry;
import com.github.tilcob.game.save.SaveManager;
import com.github.tilcob.game.save.SaveService;
import com.github.tilcob.game.save.registry.ChestRegistry;
import com.github.tilcob.game.save.states.GameState;
import com.github.tilcob.game.save.states.StateManager;
import com.github.tilcob.game.ui.UiServices;
import com.github.tilcob.game.yarn.CutsceneYarnRuntime;
import com.github.tilcob.game.yarn.DialogYarnRuntime;
import com.github.tilcob.game.yarn.QuestYarnRuntime;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class GameServicesBuilder {
    private final InternalFileHandleResolver resolver;
    private final SaveManager saveManager;
    private final Supplier<EntityLookup> entityLookupSupplier;

    public GameServicesBuilder(
        InternalFileHandleResolver resolver,
        SaveManager saveManager,
        Supplier<EntityLookup> entityLookupSupplier
    ) {
        this.resolver = resolver;
        this.saveManager = saveManager;
        this.entityLookupSupplier = entityLookupSupplier;
    }

    public Components build() {
        GameEventBus eventBus = new GameEventBus();
        ItemEntityRegistry itemEntityRegistry = new ItemEntityRegistry(eventBus);
        ChestRegistry chestRegistry = new ChestRegistry();
        StateManager stateManager = new StateManager(new GameState());
        SaveService saveService = new SaveService(saveManager, stateManager, chestRegistry);
        AssetManager assetManager = new AssetManager(resolver);
        AudioManager audioManager = AudioModule.create(assetManager);

        Map<String, Quest> allQuests = new HashMap<>();
        Map<String, DialogData> allQuestDialogs = new HashMap<>();
        Map<String, DialogData> allDialogs = new HashMap<>();
        Map<String, CutsceneData> allCutscenes = new HashMap<>();

        QuestModule.QuestServices questServices = QuestModule.create(
            eventBus,
            allDialogs,
            audioManager,
            entityLookupSupplier);

        DialogModule.DialogServices dialogServices = DialogModule.create(
            questServices.yarnRuntime(),
            questServices.flowBootstrap());

        CutsceneModule.CutsceneServices cutsceneServices = CutsceneModule.create(
            questServices.yarnRuntime(),
            questServices.flowBootstrap());

        InventoryService inventoryService = new InventoryService(eventBus);
        UiServices uiServices = new UiServices(audioManager);
        questServices.questLifecycleService().setInventoryService(inventoryService);

        return new Components(
            eventBus,
            itemEntityRegistry,
            chestRegistry,
            stateManager,
            saveService,
            assetManager,
            audioManager,
            allQuests,
            allQuestDialogs,
            allDialogs,
            allCutscenes,
            questServices.questYarnRegistry(),
            dialogServices.dialogRepository(),
            cutsceneServices.cutsceneRepository(),
            dialogServices.dialogYarnRuntime(),
            cutsceneServices.cutsceneYarnRuntime(),
            questServices.questYarnRuntime(),
            questServices.questManager(),
            questServices.questLifecycleService(),
            questServices.questRewardService(),
            inventoryService,
            questServices.flowBootstrap(),
            uiServices);
    }

    public record Components(
        GameEventBus eventBus,
        ItemEntityRegistry itemEntityRegistry,
        ChestRegistry chestRegistry,
        StateManager stateManager,
        SaveService saveService,
        AssetManager assetManager,
        AudioManager audioManager,
        Map<String, Quest> allQuests,
        Map<String, DialogData> allQuestDialogs,
        Map<String, DialogData> allDialogs,
        Map<String, CutsceneData> allCutscenes,
        QuestYarnRegistry questYarnRegistry,
        DialogRepository dialogRepository,
        CutsceneRepository cutsceneRepository,
        DialogYarnRuntime dialogYarnRuntime,
        CutsceneYarnRuntime cutsceneYarnRuntime,
        QuestYarnRuntime questYarnRuntime,
        QuestManager questManager,
        QuestLifecycleService questLifecycleService,
        QuestRewardService questRewardService,
        InventoryService inventoryService,
        FlowBootstrap flowBootstrap,
        UiServices uiServices
    ) {
    }
}
