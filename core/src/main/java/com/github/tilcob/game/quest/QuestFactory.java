package com.github.tilcob.game.quest;

import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.item.ItemDefinitionRegistry;
import com.github.tilcob.game.quest.step.CollectItemStep;
import com.github.tilcob.game.quest.step.KillStep;
import com.github.tilcob.game.quest.step.QuestStep;
import com.github.tilcob.game.quest.step.TalkStep;

import java.util.ArrayList;
import java.util.List;

public class QuestFactory {
    private final GameEventBus eventBus;
    private final QuestYarnRegistry questRegistry;

    public QuestFactory(GameEventBus eventBus, QuestYarnRegistry questRegistry) {
        this.eventBus = eventBus;
        this.questRegistry = questRegistry;
    }

    public Quest createQuest(QuestDefinition definition) {
        QuestReward reward = createReward(definition.reward());
        Quest quest = new Quest(definition.questId(), definition.title(), definition.description(), reward);

        for (var step : definition.steps()) {
            quest.getSteps().add(createStep(step));
        }
        return quest;
    }

    private QuestReward createReward(QuestDefinition.RewardDefinition reward) {
        if (reward == null) return null;
        List<String> items = new ArrayList<>();
        if (reward.items() != null) reward.items().forEach(
            item -> items.add(ItemDefinitionRegistry.resolveId(item)));
        return new QuestReward(reward.money(), items);
    }

    public QuestStep createStep(QuestDefinition.StepDefinition step) {
        return switch (step.type()) {
            case "talk"  -> new TalkStep(step.npc(), eventBus);
            case "collect" -> new CollectItemStep(ItemDefinitionRegistry.resolveId(step.itemId()), step.amount(), eventBus);
            case "kill" -> new KillStep(step.enemy(), step.amount(), eventBus);
            default -> throw new IllegalArgumentException("Unknown step type: " + step.type());
        };
    }

    public Quest create(String questId) {
        if (questRegistry.isEmpty()) questRegistry.loadAll();
        QuestDefinition definition = questRegistry.getQuestDefinition(questId);
        if (definition != null) return createQuest(definition);
        throw new IllegalArgumentException("Quest not found: " + questId);
    }
}
