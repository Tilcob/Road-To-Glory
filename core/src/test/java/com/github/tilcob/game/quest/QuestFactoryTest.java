package com.github.tilcob.game.quest;

import com.github.tilcob.game.test.HeadlessGdxTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuestFactoryTest extends HeadlessGdxTest {

    @Test
    void createQuestAllowsNullRewardItems() {
        QuestYarnRegistry registry = new QuestYarnRegistry("quests/index.json");
        QuestFactory factory = new QuestFactory(registry);
        QuestDefinition.RewardDefinition rewardDefinition = new QuestDefinition.RewardDefinition(25, null);
        QuestDefinition questDefinition = new QuestDefinition(
            "Reward_Quest",
            "Reward Quest",
            "Reward test",
            "reward_start",
            List.of(),
            QuestDefinition.RewardTiming.GIVER,
            rewardDefinition
        );

        Quest quest = factory.createQuest(questDefinition);

        assertNotNull(quest.getReward());
        assertEquals(25, quest.getReward().money());
        assertTrue(quest.getReward().items().isEmpty());
    }

    @Test
    void createUsesRegistryDefinitions() {
        QuestYarnRegistry registry = new QuestYarnRegistry("quests/index.json");
        QuestFactory factory = new QuestFactory(registry);
        registry.loadAll();

        QuestDefinition definition = registry.getQuestDefinition("welcome_to_town");
        assertNotNull(definition);
        Quest quest = factory.createQuest(definition);

        assertNotNull(quest);
        assertEquals("welcome_to_town", quest.getQuestId());
        assertEquals("Welcome to Town", quest.getTitle());
        assertEquals(1, quest.getTotalStages());
        assertNotNull(quest.getReward());
        assertEquals(50, quest.getReward().money());
        assertEquals(1, quest.getReward().items().size());
    }
}
