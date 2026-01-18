package com.github.tilcob.game.quest;

import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.item.ItemDefinitionRegistry;
import com.github.tilcob.game.item.ItemDefinitions;
import com.github.tilcob.game.quest.step.CollectItemStep;
import com.github.tilcob.game.quest.step.KillStep;
import com.github.tilcob.game.quest.step.QuestStep;
import com.github.tilcob.game.quest.step.TalkStep;

import java.util.ArrayList;
import java.util.List;

public class QuestFactory {
    private final GameEventBus eventBus;
    private final QuestRepository questRepository;

    public QuestFactory(GameEventBus eventBus, QuestRepository questRepository) {
        this.eventBus = eventBus;
        this.questRepository = questRepository;
    }

    public Quest createQuestFromJson(QuestJson json) {
        QuestReward reward = createReward(json.rewards());
        Quest quest = new Quest(json.questId(), json.title(), json.description(), reward);

        for (QuestJson.StepJson step : json.steps()) {
            quest.getSteps().add(createStep(step));
        }
        return quest;
    }

    private QuestReward createReward(QuestJson.RewardJson rewardJson) {
        if (rewardJson == null) return null;
        List<String> items = new ArrayList<>();
        if (rewardJson.items() != null) rewardJson.items().forEach(
            item -> items.add(ItemDefinitionRegistry.resolveId(item)));
        return new QuestReward(rewardJson.money(), items);
    }

    public QuestStep createStep(QuestJson.StepJson step) {
        return switch (step.type()) {
            case "talk"  -> new TalkStep(step.npc(), eventBus);
            case "collect" -> new CollectItemStep(step.itemId(), step.amount(), eventBus);
            case "kill" -> new KillStep(step.enemy(), step.amount(), eventBus);
            default -> throw new IllegalArgumentException("Unknown step type: " + step.type());
        };
    }

    public Quest create(String questId) {
        if (questRepository.isEmpty()) questRepository.loadAll();
        QuestJson definition = questRepository.getQuestDefinition(questId);
        if (definition != null) return createQuestFromJson(definition);
        throw new IllegalArgumentException("Quest not found: " + questId);
    }
}
