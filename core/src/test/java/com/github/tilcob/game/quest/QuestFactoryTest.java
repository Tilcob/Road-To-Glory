package com.github.tilcob.game.quest;

import com.github.tilcob.game.assets.QuestAsset;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.test.HeadlessGdxTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuestFactoryTest extends HeadlessGdxTest {

    @Test
    void createQuestFromJsonAllowsNullRewardItems() {
        QuestFactory factory = new QuestFactory(new GameEventBus());
        QuestFactory.RewardJson rewardJson = new QuestFactory.RewardJson(25, null);
        QuestFactory.QuestJson questJson = new QuestFactory.QuestJson(
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
    void loadAllReadsQuestAssetJson() {
        QuestFactory factory = new QuestFactory(new GameEventBus());

        Quest quest = factory.loadAll(QuestAsset.welcome_to_town).get("welcome_to_town");

        assertNotNull(quest);
        assertEquals("welcome_to_town", quest.getQuestId());
        assertEquals("Welcome to Town", quest.getTitle());
        assertEquals(1, quest.getSteps().size());
        assertNotNull(quest.getReward());
        assertEquals(50, quest.getReward().money());
        assertEquals(1, quest.getReward().items().size());
    }
}
