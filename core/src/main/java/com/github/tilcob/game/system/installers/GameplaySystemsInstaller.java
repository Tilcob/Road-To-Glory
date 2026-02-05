package com.github.tilcob.game.system.installers;

import com.badlogic.ashley.core.Engine;
import com.github.tilcob.game.cutscene.CutsceneData;
import com.github.tilcob.game.dialog.DialogData;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.inventory.InventoryService;
import com.github.tilcob.game.quest.QuestLifecycleService;
import com.github.tilcob.game.quest.QuestManager;
import com.github.tilcob.game.quest.QuestRewardService;
import com.github.tilcob.game.quest.QuestYarnRegistry;
import com.github.tilcob.game.save.states.StateManager;
import com.github.tilcob.game.system.*;
import com.github.tilcob.game.tiled.TiledManager;
import com.github.tilcob.game.yarn.CutsceneYarnRuntime;
import com.github.tilcob.game.yarn.DialogYarnRuntime;
import com.github.tilcob.game.yarn.QuestYarnRuntime;

import java.util.Map;

public class GameplaySystemsInstaller implements SystemInstaller {
        private final TiledManager tiledManager;
        private final GameEventBus eventBus;
        private final StateManager stateManager;
        private final QuestManager questManager;
        private final InventoryService inventoryService;
        private final QuestLifecycleService questLifecycleService;
        private final QuestRewardService questRewardService;
        private final DialogYarnRuntime dialogYarnRuntime;
        private final CutsceneYarnRuntime cutsceneYarnRuntime;
        private final QuestYarnRuntime questYarnRuntime;
        private final QuestYarnRegistry questYarnRegistry;
        private final Map<String, DialogData> allDialogs;
        private final Map<String, CutsceneData> allCutscenes;

        public GameplaySystemsInstaller(
                    TiledManager tiledManager,
                    GameEventBus eventBus,
                    StateManager stateManager,
                    QuestManager questManager,
                    InventoryService inventoryService,
                    QuestLifecycleService questLifecycleService,
                    QuestRewardService questRewardService,
                    DialogYarnRuntime dialogYarnRuntime,
                    CutsceneYarnRuntime cutsceneYarnRuntime,
                    QuestYarnRuntime questYarnRuntime,
                    QuestYarnRegistry questYarnRegistry,
                    Map<String, DialogData> allDialogs,
                    Map<String, CutsceneData> allCutscenes) {
            this.tiledManager = tiledManager;
            this.eventBus = eventBus;
            this.stateManager = stateManager;
            this.questManager = questManager;
            this.inventoryService = inventoryService;
            this.questLifecycleService = questLifecycleService;
            this.questRewardService = questRewardService;
            this.dialogYarnRuntime = dialogYarnRuntime;
            this.cutsceneYarnRuntime = cutsceneYarnRuntime;
            this.questYarnRuntime = questYarnRuntime;
            this.questYarnRegistry = questYarnRegistry;
            this.allDialogs = allDialogs;
            this.allCutscenes = allCutscenes;
        }

        @Override
        public void install(Engine engine) {
            engine.addSystem(withPriority(
                            new MapChangeSystem(tiledManager, eventBus, stateManager),
                            SystemOrder.GAMEPLAY));
            engine.addSystem(withPriority(
                            new InventorySystem(eventBus, questManager, inventoryService),
                            SystemOrder.GAMEPLAY));
            engine.addSystem(withPriority(new ExpDistributionSystem(eventBus), SystemOrder.GAMEPLAY));
            engine.addSystem(withPriority(new SkillSystem(eventBus), SystemOrder.GAMEPLAY));
            engine.addSystem(withPriority(new EquipmentSystem(eventBus, inventoryService), SystemOrder.GAMEPLAY));
            engine.addSystem(withPriority(new EquipmentStatModifierSystem(eventBus), SystemOrder.GAMEPLAY));
            engine.addSystem(withPriority(new StatModifierDurationSystem(eventBus), SystemOrder.GAMEPLAY));
            engine.addSystem(withPriority(new LevelUpSystem(eventBus), SystemOrder.GAMEPLAY));
            engine.addSystem(withPriority(new StatRecalcSystem(eventBus), SystemOrder.GAMEPLAY));
            engine.addSystem(withPriority(
                            new ChestSystem(inventoryService, eventBus, questManager),
                            SystemOrder.GAMEPLAY));
            engine.addSystem(withPriority(new QuestSystem(
                            eventBus, questLifecycleService),
                            SystemOrder.GAMEPLAY));
            engine.addSystem(withPriority(new RewardSystem(eventBus, questRewardService),
                            SystemOrder.GAMEPLAY));
            engine.addSystem(withPriority(
                            new QuestRewardSchedulerSystem(
                                            eventBus,
                                            questLifecycleService),
                            SystemOrder.GAMEPLAY));
            engine.addSystem(withPriority(new DialogSessionSystem(eventBus), SystemOrder.GAMEPLAY));
            engine.addSystem(withPriority(new StartDialogSystem(eventBus), SystemOrder.GAMEPLAY));
            engine.addSystem(withPriority(new DialogConsequenceSystem(
                            eventBus,
                            questManager,
                            questLifecycleService),
                            SystemOrder.GAMEPLAY));
            engine.addSystem(withPriority(new OcclusionSystem(), SystemOrder.GAMEPLAY));
            engine.addSystem(withPriority(
                            new DialogQuestBridgeSystem(
                                            eventBus,
                                            questManager),
                            SystemOrder.GAMEPLAY));
            engine.addSystem(withPriority(
                new OverheadIndicatorAttachSystem(),
                SystemOrder.GAMEPLAY));
            engine.addSystem(withPriority(
                new OverheadIndicatorStateSystem(allDialogs, questYarnRegistry),
                SystemOrder.GAMEPLAY));
            engine.addSystem(withPriority(
                new IndicatorCommandLifetimeSystem(),
                SystemOrder.GAMEPLAY));
            engine.addSystem(withPriority(
                            new DialogSystem(eventBus, allDialogs, dialogYarnRuntime),
                            SystemOrder.GAMEPLAY));
            engine.addSystem(withPriority(
                            new CutsceneSystem(eventBus, allCutscenes, cutsceneYarnRuntime),
                            SystemOrder.GAMEPLAY));
            engine.addSystem(withPriority(
                            new YarnScopeCleanupSystem(dialogYarnRuntime, questYarnRuntime),
                            SystemOrder.GAMEPLAY));
        }
}
