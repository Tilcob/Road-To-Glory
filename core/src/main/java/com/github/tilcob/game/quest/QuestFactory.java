package com.github.tilcob.game.quest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tilcob.game.assets.QuestAsset;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.item.ItemType;
import com.github.tilcob.game.quest.step.CollectItemStep;
import com.github.tilcob.game.quest.step.KillStep;
import com.github.tilcob.game.quest.step.QuestStep;
import com.github.tilcob.game.quest.step.TalkStep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestFactory {
    private final GameEventBus eventBus;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, QuestJson> definitions = new HashMap<>();

    public QuestFactory(GameEventBus eventBus) {
        this.eventBus = eventBus;
    }

    public Map<String, Quest> loadAll(QuestAsset asset) {
        try {
            QuestJson questJson = mapper.readValue(asset.getFile().readString(), QuestJson.class);
            Map<String, Quest> quests = new HashMap<>();

            validateQuestId(asset, questJson);
            definitions.put(questJson.questId, questJson);
            quests.put(questJson.questId, createQuestFromJson(questJson));

            return quests;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Quest createQuestFromJson(QuestJson json) {
        QuestReward reward = createReward(json.rewards);
        Quest quest = new Quest(json.questId, json.title, json.description, reward);

        for (StepJson step : json.steps) {
            quest.getSteps().add(createStep(step));
        }
        return quest;
    }

    private QuestReward createReward(RewardJson rewardJson) {
        if (rewardJson == null) {
            return null;
        }
        List<ItemType> items = new ArrayList<>();
        if (rewardJson.items != null) rewardJson.items.forEach(item -> items.add(ItemType.valueOf(item)));
        return new QuestReward(rewardJson.gold, items);
    }

    public QuestStep createStep(StepJson step) {
        return switch (step.type) {
            case "talk"  -> new TalkStep(step.npc, eventBus);
            case "collect" -> new CollectItemStep(ItemType.valueOf(step.item), step.amount, eventBus);
            case "kill" -> new KillStep(step.enemy, step.amount, eventBus);
            default -> throw new IllegalArgumentException("Unknown step type: " + step.type);
        };
    }

    public Quest create(String questId) {
        if (definitions.isEmpty()) {
            loadAllDefinitions();
        }
        QuestJson definition = definitions.get(questId);
        if (definition != null) return createQuestFromJson(definition);

        throw new IllegalArgumentException("Quest not found: " + questId);
    }

    private void loadAllDefinitions() {
        for (QuestAsset asset : QuestAsset.values()) {
            QuestJson questJson;
            try {
                questJson = mapper.readValue(asset.getFile().readString(), QuestJson.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            validateQuestId(asset, questJson);
            definitions.put(questJson.questId, questJson);
        }
    }

    private void validateQuestId(QuestAsset asset, QuestJson questJson) {
        if (!asset.name().equals(questJson.questId)) {
            throw new IllegalStateException("Quest ID mismatch for asset " + asset.name()
                + ": expected " + asset.name() + " but found " + questJson.questId);
        }
    }

    public record QuestJson(String questId, String title, String description, List<StepJson> steps, RewardJson rewards) { }

    public record StepJson(String type, String npc, String item, int amount, String enemy) { }

    public record RewardJson(int gold, List<String> items) { }
}
