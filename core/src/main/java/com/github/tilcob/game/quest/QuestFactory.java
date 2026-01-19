package com.github.tilcob.game.quest;

import com.github.tilcob.game.item.ItemDefinitionRegistry;

import java.util.ArrayList;
import java.util.List;

public class QuestFactory {
    private final QuestYarnRegistry questRegistry;

    public QuestFactory(QuestYarnRegistry questRegistry) {
        this.questRegistry = questRegistry;
    }

    public Quest createQuest(QuestDefinition definition) {
        QuestReward reward = createReward(definition.reward());
        int stageCount = definition.steps() == null ? 0 : definition.steps().size();
        return new Quest(definition.questId(), definition.displayName(), definition.journalText(), reward, stageCount);
    }

    private QuestReward createReward(QuestDefinition.RewardDefinition reward) {
        if (reward == null) return null;
        List<String> items = new ArrayList<>();
        if (reward.items() != null) reward.items().forEach(
            item -> items.add(ItemDefinitionRegistry.resolveId(item)));
        return new QuestReward(reward.money(), items);
    }

    public Quest create(String questId) {
        if (questRegistry.isEmpty()) questRegistry.loadAll();
        QuestDefinition definition = questRegistry.getQuestDefinition(questId);
        if (definition != null) return createQuest(definition);
        throw new IllegalArgumentException("Quest not found: " + questId);
    }
}
