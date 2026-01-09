package com.github.tilcob.game.quest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tilcob.game.assets.QuestAsset;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.quest.step.QuestStep;
import com.github.tilcob.game.quest.step.TalkStep;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestFactory {
    private final GameEventBus eventBus;
    private final ObjectMapper mapper = new ObjectMapper();

    public QuestFactory(GameEventBus eventBus) {
        this.eventBus = eventBus;
    }

    public Map<String, Quest> loadAll(QuestAsset asset) {
        try {
            QuestFile file = mapper.readValue(asset.getFile().readString(), QuestFile.class);
            Map<String, Quest> quests = new HashMap<>();

            for (QuestJson q : file.quests) {
                quests.put(q.questId, createQuestFromJson(q));
            }
            return quests;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Quest createQuestFromJson(QuestJson json) {
        Quest quest = new Quest(json.questId);

        for (StepJson step : json.steps) {
            quest.getSteps().add(createStep(step));
        }
        return quest;
    }

    public QuestStep createStep(StepJson step) {
        return switch (step.type) {
            case "talk"  -> new TalkStep(step.npc, eventBus);
            //case "collect" ->
            default -> throw new IllegalArgumentException("Unknown step type: " + step.type);
        };
    }

    public Quest create(String questId) {
        for (QuestAsset asset : QuestAsset.values()) {
            QuestFile file;
            try {
                file = mapper.readValue(asset.getFile().readString(), QuestFile.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (QuestJson q : file.quests) {
                if (q.questId.equals(questId)) {
                    return createQuestFromJson(q);
                }
            }
        }

        throw new IllegalArgumentException("Quest not found: " + questId);
    }


    public record QuestFile(List<QuestJson> quests) { }

    public record QuestJson(String questId, List<StepJson> steps) { }

    public record StepJson(String type, String npc, String item, int amount, String enemy) { }
}
