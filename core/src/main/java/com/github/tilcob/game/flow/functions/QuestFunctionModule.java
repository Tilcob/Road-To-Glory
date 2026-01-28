package com.github.tilcob.game.flow.functions;

import com.badlogic.gdx.utils.ObjectIntMap;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.flow.FlowContext;
import com.github.tilcob.game.flow.FunctionCall;
import com.github.tilcob.game.flow.FunctionRegistry;
import com.github.tilcob.game.item.ItemDefinitionRegistry;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.QuestState;

public final class QuestFunctionModule {

    public void register(FunctionRegistry registry) {
        registry.register("$quest_is_active", (call, args) -> questState(call, args) == QuestState.IN_PROGRESS);
        registry.register("$quest_is_completed", (call, args) -> questState(call, args) == QuestState.COMPLETED);
        registry.register("$quest_stage", this::currentStage);
        registry.register("$flag", this::flagValue);
        registry.register("$counter", this::counterValue);
        registry.register("$has_item", this::hasItem);
    }

    private QuestState questState(FunctionCall call, FlowContext context) {
        String questId = call.arguments().get(0);
        if (context.player() == null || questId == null) return QuestState.NOT_STARTED;
        QuestLog questLog = QuestLog.MAPPER.get(context.player());
        if (questLog == null) return QuestState.NOT_STARTED;
        return questLog.getQuestStateById(questId);
    }

    private Integer currentStage(FunctionCall call, FlowContext context) {
        String questId = call.arguments().get(0);
        if (context.player() == null || questId == null) return -1;

        QuestLog questLog = QuestLog.MAPPER.get(context.player());
        if (questLog == null) return -1;

        Quest quest = questLog.getQuestById(questId);
        return quest == null ? -1 : quest.getCurrentStep();
    }

    private Boolean flagValue(FunctionCall call, FlowContext context) {
        String flag = call.arguments().get(0);
        if (context.player() == null || flag == null) return false;
        DialogFlags flags = DialogFlags.MAPPER.get(context.player());
        return flags != null && flags.get(flag);
    }

    private Integer counterValue(FunctionCall call, FlowContext context) {
        String counter = call.arguments().get(0);
        if (context.player() == null || counter == null) return 0;
        Counters counters = Counters.MAPPER.get(context.player());
        return counters == null ? 0 : counters.get(counter);
    }

    private Boolean hasItem(FunctionCall call, FlowContext context) {
        String itemId = call.arguments().get(0);
        if (context.player() == null || itemId == null) return false;
        Inventory inventory = Inventory.MAPPER.get(context.player());
        if (inventory == null) return false;

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
}
