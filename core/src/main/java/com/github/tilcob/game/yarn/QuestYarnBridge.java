package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.event.AddQuestEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.QuestCompletedEvent;
import com.github.tilcob.game.event.UpdateQuestLogEvent;
import com.github.tilcob.game.item.ItemDefinitionRegistry;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.QuestState;

public class QuestYarnBridge {
    private final GameEventBus eventBus;
    private final boolean allowRewards;

    public QuestYarnBridge(GameEventBus eventBus, boolean allowRewards) {
        this.eventBus = eventBus;
        this.allowRewards = allowRewards;
    }

    public void registerAll(YarnRuntime runtime) {
        registerCommands(runtime);
        registerFunctions(runtime);
    }

    private void registerCommands(YarnCommandRegistry registry) {
        registry.register("quest_start", this::startQuest);
        registry.register("quest_complete", this::completeQuest);
        registry.register("quest_stage", this::setQuestStage);
        if (allowRewards) {
            registry.register("give_money", this::giveMoney);
            registry.register("give_item", this::giveItem);
        }
        registry.register("set_flag", this::setFlag);
        registry.register("inc_counter", this::incrementCounter);
    }

    private void registerFunctions(YarnFunctionRegistry registry) {
        registry.register("$quest_is_active", (player, args) -> questState(player, args) == QuestState.IN_PROGRESS);
        registry.register("$quest_is_completed", (player, args) -> questState(player, args) == QuestState.COMPLETED);
        registry.register("$quest_stage", (player, args) -> currentStage(player, args));
        registry.register("$flag", (player, args) -> flagValue(player, args));
        registry.register("$counter", (player, args) -> counterValue(player, args));
        registry.register("$has_item", (player, args) -> hasItem(player, args));
    }

    private void startQuest(Entity player, String[] args) {
        String questId = firstArg(args);
        if (player == null || questId == null) {
            return;
        }
        eventBus.fire(new AddQuestEvent(player, questId));
    }

    private void completeQuest(Entity player, String[] args) {
        String questId = firstArg(args);
        if (player == null || questId == null) {
            return;
        }
        QuestLog questLog = QuestLog.MAPPER.get(player);
        if (questLog != null) {
            Quest quest = questLog.getQuestById(questId);
            if (quest != null) {
                if (!quest.isCompleted()) quest.setCurrentStep(quest.getTotalStages());
                eventBus.fire(new UpdateQuestLogEvent(player));
                quest.setCompletionNotified(true);
            }
        }
        eventBus.fire(new QuestCompletedEvent(player, questId));
    }

    private void setQuestStage(Entity player, String[] args) {
        if (player == null || args == null || args.length < 2) return;
        String questId = args[0];
        int stage = parseInt(args[1], -1);
        if (questId == null || stage < 0) return;
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
    }

    private void giveMoney(Entity player, String[] args) {
        if (player == null || args == null || args.length == 0) return;
        int amount = parseInt(args[0], 0);
        if (amount <= 0) return;
        Wallet wallet = Wallet.MAPPER.get(player);
        if (wallet == null) {
            wallet = new Wallet();
            player.add(wallet);
        }
        wallet.earn(amount);
    }

    private void giveItem(Entity player, String[] args) {
        if (player == null || args == null || args.length == 0) return;
        String itemId = args[0];
        int count = args.length > 1 ? parseInt(args[1], 1) : 1;
        if (itemId == null || itemId.isBlank() || count <= 0) return;
        Inventory inventory = Inventory.MAPPER.get(player);
        if (inventory == null) {
            inventory = new Inventory();
            player.add(inventory);
        }
        String resolved = ItemDefinitionRegistry.resolveId(itemId);
        for (int i = 0; i < count; i++) inventory.getItemsToAdd().add(resolved);
    }

    private void setFlag(Entity player, String[] args) {
        if (player == null || args == null || args.length == 0) {
            return;
        }
        String flag = args[0];
        boolean value = args.length <= 1 || Boolean.parseBoolean(args[1]);
        if (flag == null || flag.isBlank()) {
            return;
        }
        DialogFlags flags = DialogFlags.MAPPER.get(player);
        if (flags == null) {
            flags = new DialogFlags();
            player.add(flags);
        }
        flags.set(flag, value);
    }

    private void incrementCounter(Entity player, String[] args) {
        if (player == null || args == null || args.length == 0) {
            return;
        }
        String counter = args[0];
        int amount = args.length > 1 ? parseInt(args[1], 1) : 1;
        if (counter == null || counter.isBlank()) {
            return;
        }
        Counters counters = Counters.MAPPER.get(player);
        if (counters == null) {
            counters = new Counters();
            player.add(counters);
        }
        counters.increment(counter, amount);
    }

    private QuestState questState(Entity player, String[] args) {
        String questId = firstArg(args);
        if (player == null || questId == null) {
            return QuestState.NOT_STARTED;
        }
        QuestLog questLog = QuestLog.MAPPER.get(player);
        if (questLog == null) {
            return QuestState.NOT_STARTED;
        }
        return questLog.getQuestStateById(questId);
    }

    private Integer currentStage(Entity player, String[] args) {
        String questId = firstArg(args);
        if (player == null || questId == null) {
            return -1;
        }
        QuestLog questLog = QuestLog.MAPPER.get(player);
        if (questLog == null) {
            return -1;
        }
        Quest quest = questLog.getQuestById(questId);
        return quest == null ? -1 : quest.getCurrentStep();
    }

    private Boolean flagValue(Entity player, String[] args) {
        String flag = firstArg(args);
        if (player == null || flag == null) {
            return false;
        }
        DialogFlags flags = DialogFlags.MAPPER.get(player);
        return flags != null && flags.get(flag);
    }

    private Integer counterValue(Entity player, String[] args) {
        String counter = firstArg(args);
        if (player == null || counter == null) {
            return 0;
        }
        Counters counters = Counters.MAPPER.get(player);
        return counters == null ? 0 : counters.get(counter);
    }

    private Boolean hasItem(Entity player, String[] args) {
        String itemId = firstArg(args);
        if (player == null || itemId == null) {
            return false;
        }
        Inventory inventory = Inventory.MAPPER.get(player);
        if (inventory == null) {
            return false;
        }
        String resolved = ItemDefinitionRegistry.resolveId(itemId);
        ObjectIntMap<String> counts = new ObjectIntMap<>();
        for (var entity : inventory.getItems()) {
            Item item = Item.MAPPER.get(entity);
            counts.getAndIncrement(item.getItemId(), 0, item.getCount());
        }
        for (String pending : inventory.getItemsToAdd()) {
            counts.getAndIncrement(ItemDefinitionRegistry.resolveId(pending), 0, 1);
        }
        return counts.get(resolved, 0) > 0;
    }

    private String firstArg(String[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        String value = args[0];
        return value == null || value.isBlank() ? null : value;
    }

    private int parseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
