package com.github.tilcob.game.quest;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.dialog.DialogData;
import com.github.tilcob.game.dialog.QuestDialog;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.QuestCompletedEvent;
import com.github.tilcob.game.event.QuestRewardEvent;
import com.github.tilcob.game.event.UpdateQuestLogEvent;
import com.github.tilcob.game.inventory.InventoryService;
import com.github.tilcob.game.item.ItemDefinitionRegistry;
import com.github.tilcob.game.yarn.QuestYarnRuntime;

import java.util.Locale;
import java.util.Map;

public class QuestLifecycleService {
    private final GameEventBus eventBus;
    private final QuestYarnRegistry questYarnRegistry;
    private final QuestFactory questFactory;
    private final Map<String, DialogData> allDialogs;
    private QuestManager questManager;
    private InventoryService inventoryService;
    private boolean autoProgressRunning = false;
    private QuestYarnRuntime questYarnRuntime;

    public QuestLifecycleService(GameEventBus eventBus,
                                 QuestYarnRegistry questYarnRegistry,
                                 Map<String, DialogData> allDialogs) {
        this.eventBus = eventBus;
        this.questYarnRegistry = questYarnRegistry;
        this.questFactory = new QuestFactory(questYarnRegistry);
        this.allDialogs = allDialogs;
    }

    public void setQuestYarnRuntime(QuestYarnRuntime questYarnRuntime) {
        this.questYarnRuntime = questYarnRuntime;
    }

    public void startQuest(Entity player, String questId) {
        if (player == null || questId == null || questId.isBlank()) return;
        if (Gdx.app != null && Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
            Gdx.app.debug("QuestLifecycleService", "Start quest: " + questId);
        }
        QuestLog questLog = QuestLog.MAPPER.get(player);
        if (questLog == null) return;
        for (Quest quest : questLog.getQuests()) {
            if (quest != null && questId.equals(quest.getQuestId())) return;
        }
        Quest quest = questFactory.create(questId);
        questLog.add(quest);
        if (quest.getTotalStages() == 0) {
            quest.setCompletionNotified(true);
            eventBus.fire(new QuestCompletedEvent(player, questId));
        }
        QuestDefinition definition = questDefinitionFor(questId);
        if (definition != null && questYarnRuntime != null) {
            questYarnRuntime.executeStartNode(player, definition.startNode());
        }
        eventBus.fire(new UpdateQuestLogEvent(player));
        autoProgressQuest(player, questId);
    }

    public void setQuestStage(Entity player, String questId, int stage) {
        if (player == null || questId == null || questId.isBlank() || stage < 0) return;
        if (Gdx.app != null && Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
            Gdx.app.debug("QuestLifecycleService", "Set quest stage: " + questId + " -> " + stage);
        }
        QuestLog questLog = QuestLog.MAPPER.get(player);
        if (questLog == null) return;
        Quest quest = questLog.getQuestById(questId);
        if (quest == null) return;
        quest.setCurrentStep(Math.min(stage, quest.getTotalStages()));
        if (quest.isCompleted()) {
            quest.setCompletionNotified(true);
            eventBus.fire(new QuestCompletedEvent(player, questId));
        }
        eventBus.fire(new UpdateQuestLogEvent(player));
        autoProgressQuest(player, questId);
    }

    public void completeQuest(Entity player, String questId) {
        if (player == null || questId == null || questId.isBlank()) return;
        QuestLog questLog = QuestLog.MAPPER.get(player);
        if (questLog != null) {
            Quest quest = questLog.getQuestById(questId);
            if (quest != null) {
                if (!quest.isCompleted()) quest.setCurrentStep(quest.getTotalStages());
                quest.setCompletionNotified(true);
                eventBus.fire(new UpdateQuestLogEvent(player));
            }
        }
        eventBus.fire(new QuestCompletedEvent(player, questId));
    }

    public void notifyQuestCompletion(Entity player, Quest quest) {
        if (player == null || quest == null || !quest.isCompleted() || quest.isCompletionNotified()) return;
        quest.setCompletionNotified(true);
        eventBus.fire(new QuestCompletedEvent(player, quest.getQuestId()));
        eventBus.fire(new UpdateQuestLogEvent(player));
    }

    public void scheduleRewardFromCompletion(Entity player, String questId) {
        if (player == null || questId == null || questId.isBlank()) return;
        QuestDefinition definition = questDefinitionFor(questId);
        if (definition == null) return;
        RewardTiming timing = definition.rewardTiming();
        if (timing == RewardTiming.COMPLETION || timing == RewardTiming.AUTO) {
            eventBus.fire(new QuestRewardEvent(player, questId));
        }
    }

    public void scheduleRewardFromDialog(Entity npcEntity, Entity player) {
        if (npcEntity == null || player == null) return;
        Npc npc = Npc.MAPPER.get(npcEntity);
        if (npc == null) return;
        DialogData dialogData = allDialogs.get(npc.getName());
        if (dialogData == null) return;
        QuestDialog questDialog = dialogData.questDialog();
        if (questDialog == null || questDialog.questId() == null) return;
        QuestDefinition definition = questDefinitionFor(questDialog.questId());
        if (definition == null || definition.rewardTiming() != RewardTiming.GIVER) return;

        QuestLog questLog = QuestLog.MAPPER.get(player);
        if (questLog == null) return;
        Quest quest = questLog.getQuestById(questDialog.questId());
        if (quest == null || !quest.isCompleted() || quest.isRewardClaimed()) return;

        eventBus.fire(new QuestRewardEvent(player, questDialog.questId()));
    }

    private QuestDefinition questDefinitionFor(String questId) {
        if (questYarnRegistry.isEmpty()) questYarnRegistry.loadAll();
        return questYarnRegistry.getQuestDefinition(questId);
    }

    private void autoProgressQuest(Entity player, String questId) {
        if (autoProgressRunning) return;
        if (player == null || questId == null) return;
        if (questManager == null) return;

        QuestDefinition definition = questDefinitionFor(questId);
        if (definition == null) return;

        QuestLog questLog = QuestLog.MAPPER.get(player);
        if (questLog == null) return;

        Quest quest = questLog.getQuestById(questId);
        if (quest == null || quest.isCompleted()) return;

        autoProgressRunning = true;
        try {
            int safety = 0;
            boolean progressed;

            do {
                progressed = false;

                int stage = quest.getCurrentStep();
                if (stage < 0 || stage >= definition.steps().size()) return;

                QuestDefinition.StepDefinition step = definition.steps().get(stage);
                String type = step.type();

                if ("collect".equals(type)) {
                    String itemId = ItemDefinitionRegistry.resolveId(step.itemId());
                    int have = countInventoryItem(player, itemId);
                    if (have >= step.amount()) {
                        questManager.signal(player, "collect", itemId, have);
                        progressed = true;
                    }
                } else if ("kill".equals(type)) {
                    String enemy = step.enemy();
                    int kills = getCounter(player, killCounterKey(enemy));
                    if (kills >= step.amount()) {
                        questManager.signal(player, "kill", enemy, kills);
                        progressed = true;
                    }
                }
            } while (progressed && safety++ < 32);

        } finally {
            autoProgressRunning = false;
        }
    }

    private static int getCounter(Entity player, String key) {
        Counters counters = Counters.MAPPER.get(player);
        return counters == null ? 0 : counters.get(key);
    }

    private static String killCounterKey(String enemyName) {
        if (enemyName == null) return "kill:unknown";
        return "kill:" + enemyName.toLowerCase(Locale.ROOT);
    }

    private static int countInventoryItem(Entity player, String itemId) {
        Inventory inventory = Inventory.MAPPER.get(player);
        if (inventory == null || itemId == null) return 0;

        String resolved = ItemDefinitionRegistry.resolveId(itemId);
        int total = 0;

        for (var entity : inventory.getItems()) {
            Item item = Item.MAPPER.get(entity);
            if (item != null && resolved.equals(item.getItemId())) {
                total += item.getCount();
            }
        }
        for (String pending : inventory.getItemsToAdd()) {
            String p = ItemDefinitionRegistry.resolveId(pending);
            if (resolved.equals(p)) total += 1;
        }
        return total;
    }

    public void setQuestManager(QuestManager questManager) {
        this.questManager = questManager;
    }

    public void setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
}
