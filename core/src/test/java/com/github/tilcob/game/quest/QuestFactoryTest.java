package com.github.tilcob.game.quest;

import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.test.HeadlessGdxTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuestFactoryTest extends HeadlessGdxTest {

    @Test
    void createQuestFromJsonAllowsNullRewardItems() {
        GameEventBus eventBus = new GameEventBus();
        QuestRepository repository = new QuestRepository(eventBus, false, "quests/index.json", "quests");
        QuestFactory factory = new QuestFactory(eventBus, repository);
        QuestJson.RewardJson rewardJson = new QuestJson.RewardJson(25, null);
        QuestJson questJson = new QuestJson(
            "Reward_Quest",
            "Reward Quest",
            "Reward test",
            List.of(),
            rewardJson
        );

        Quest quest = factory.createQuestFromJson(questJson);

        assertNotNull(quest.getReward());
        assertEquals(25, quest.getReward().money());
        assertTrue(quest.getReward().items().isEmpty());
    }

    @Test
    void createUsesRepositoryDefinitions() {
        GameEventBus eventBus = new GameEventBus();
        QuestRepository repository = new QuestRepository(eventBus, false, "quests/index.json", "quests");
        QuestFactory factory = new QuestFactory(eventBus, repository);
        repository.loadAll();

        Quest quest = factory.create("welcome_to_town");

        assertNotNull(quest);
        assertEquals("welcome_to_town", quest.getQuestId());
        assertEquals("Welcome to Town", quest.getTitle());
        assertEquals(1, quest.getSteps().size());
        assertNotNull(quest.getReward());
        assertEquals(50, quest.getReward().money());
        assertEquals(1, quest.getReward().items().size());
    }
}
