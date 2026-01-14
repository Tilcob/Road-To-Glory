package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.component.Wallet;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.QuestCompletedEvent;
import com.github.tilcob.game.item.ItemType;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.QuestReward;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RewardSystemTest {

    @Test
    void grantsRewardsAndCreatesInventoryIfMissing() {
        GameEventBus eventBus = new GameEventBus();
        RewardSystem rewardSystem = new RewardSystem(eventBus);

        Entity player = new Entity();
        QuestLog questLog = new QuestLog();
        player.add(questLog);

        QuestReward reward = new QuestReward(15, List.of(ItemType.SWORD));
        Quest quest = new Quest("Reward_Quest", "Reward Quest", "Reward test", reward);
        questLog.add(quest);

        eventBus.fire(new QuestCompletedEvent(player, "Reward_Quest"));

        Wallet wallet = Wallet.MAPPER.get(player);
        assertNotNull(wallet);
        assertEquals(15, wallet.getMoney());

        Inventory inventory = Inventory.MAPPER.get(player);
        assertNotNull(inventory);
        assertTrue(inventory.getItemsToAdd().contains(ItemType.SWORD, true));

        rewardSystem.dispose();
    }
}
