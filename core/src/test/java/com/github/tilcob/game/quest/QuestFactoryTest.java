package com.github.tilcob.game.quest;

import com.github.tilcob.game.event.GameEventBus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestFactoryTest {

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
        assertEquals(25, quest.getReward().gold());
        assertTrue(quest.getReward().items().isEmpty());
    }
}
