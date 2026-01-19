package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.component.Wallet;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.QuestRewardEvent;
import com.github.tilcob.game.event.RewardGrantedEvent;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.QuestReward;
import com.github.tilcob.game.quest.QuestYarnRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class RewardSystemTest {

    @Test
    void grantsRewardsAndCreatesInventoryIfMissing() {
        GameEventBus eventBus = new GameEventBus();
        QuestYarnRegistry registry = new QuestYarnRegistry("quests/quests_index.yarn");
        RewardSystem rewardSystem = new RewardSystem(eventBus, registry);

        Entity player = new Entity();
        QuestLog questLog = new QuestLog();
        player.add(questLog);

        QuestReward reward = new QuestReward(15, List.of("sword"));
        Quest quest = new Quest("Reward_Quest", "Reward Quest", "Reward test", reward);
        questLog.add(quest);

        quest.setCurrentStep(quest.getSteps().size());
        eventBus.fire(new QuestRewardEvent(player, "Reward_Quest"));

        Wallet wallet = Wallet.MAPPER.get(player);
        assertNotNull(wallet);
        assertEquals(15, wallet.getMoney());

        Inventory inventory = Inventory.MAPPER.get(player);
        assertNotNull(inventory);
        assertTrue(inventory.getItemsToAdd().contains("sword", true));
        assertTrue(quest.isRewardClaimed());

        rewardSystem.dispose();
    }

    @Test
    void doesNotGrantRewardsWhenQuestIsIncomplete() {
        GameEventBus eventBus = new GameEventBus();
        QuestYarnRegistry registry = new QuestYarnRegistry("quests/quests_index.yarn");
        RewardSystem rewardSystem = new RewardSystem(eventBus, registry);

        Entity player = new Entity();
        QuestLog questLog = new QuestLog();
        player.add(questLog);

        QuestReward reward = new QuestReward(10, List.of("boots"));
        Quest quest = new Quest("Incomplete_Quest", "Incomplete Quest", "Not done", reward);
        quest.getSteps().add(new TestStep(false));
        questLog.add(quest);

        eventBus.fire(new QuestRewardEvent(player, "Incomplete_Quest"));

        assertNull(Wallet.MAPPER.get(player));
        assertNull(Inventory.MAPPER.get(player));
        assertFalse(quest.isRewardClaimed());

        rewardSystem.dispose();
    }

    @Test
    void skipsRewardGrantWhenAlreadyClaimedOrEmpty() {
        GameEventBus eventBus = new GameEventBus();
        QuestYarnRegistry registry = new QuestYarnRegistry("quests/quests_index.yarn");
        RewardSystem rewardSystem = new RewardSystem(eventBus, registry);

        Entity player = new Entity();
        QuestLog questLog = new QuestLog();
        player.add(questLog);

        QuestReward reward = new QuestReward(0, List.of());
        Quest quest = new Quest("Empty_Quest", "Empty Quest", "Nothing", reward);
        quest.setCurrentStep(quest.getSteps().size());
        quest.setRewardClaimed(true);
        questLog.add(quest);

        AtomicBoolean rewardGranted = new AtomicBoolean(false);
        eventBus.subscribe(RewardGrantedEvent.class, event -> rewardGranted.set(true));

        eventBus.fire(new QuestRewardEvent(player, "Empty_Quest"));

        assertNull(Wallet.MAPPER.get(player));
        assertNull(Inventory.MAPPER.get(player));
        assertTrue(quest.isRewardClaimed());
        assertFalse(rewardGranted.get());

        rewardSystem.dispose();
    }

    private static final class TestStep implements com.github.tilcob.game.quest.step.QuestStep {
        private final boolean completed;

        private TestStep(boolean completed) {
            this.completed = completed;
        }

        @Override
        public boolean completed() {
            return completed;
        }

        @Override
        public void start() {
        }

        @Override
        public void end() {
        }
    }
}
