package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.component.Wallet;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.QuestRewardEvent;
import com.github.tilcob.game.event.RewardGrantedEvent;
import com.github.tilcob.game.event.UpdateQuestLogEvent;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.QuestReward;
import com.github.tilcob.game.quest.QuestRewardService;
import com.github.tilcob.game.quest.QuestYarnRegistry;
import com.github.tilcob.game.test.HeadlessGdxTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RewardSystemTest extends HeadlessGdxTest {

    @Test
    void grantsRewardsAndCreatesInventoryIfMissing() {
        GameEventBus eventBus = new GameEventBus();
        QuestYarnRegistry registry = new QuestYarnRegistry("tests/quests_test/index.json", "tests/quests_test");
        QuestRewardService questRewardService = new QuestRewardService(eventBus, registry);
        RewardSystem rewardSystem = new RewardSystem(eventBus, questRewardService);

        Entity player = new Entity();
        QuestLog questLog = new QuestLog();
        player.add(questLog);

        QuestReward reward = new QuestReward(15, List.of("sword"));
        Quest quest = new Quest("Reward_Quest", "Reward Quest", "Reward test", reward, 1);
        questLog.add(quest);

        quest.setCurrentStep(quest.getTotalStages());
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
        QuestYarnRegistry registry = new QuestYarnRegistry("tests/quests_test/index.json", "tests/quests_test");
        QuestRewardService questRewardService = new QuestRewardService(eventBus, registry);
        RewardSystem rewardSystem = new RewardSystem(eventBus, questRewardService);

        Entity player = new Entity();
        QuestLog questLog = new QuestLog();
        player.add(questLog);

        QuestReward reward = new QuestReward(10, List.of("boots"));
        Quest quest = new Quest("Incomplete_Quest", "Incomplete Quest", "Not done", reward, 1);
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
        QuestYarnRegistry registry = new QuestYarnRegistry("tests/quests_test/index.json", "tests/quests_test");
        QuestRewardService questRewardService = new QuestRewardService(eventBus, registry);
        RewardSystem rewardSystem = new RewardSystem(eventBus, questRewardService);

        Entity player = new Entity();
        QuestLog questLog = new QuestLog();
        player.add(questLog);

        QuestReward reward = new QuestReward(0, List.of());
        Quest quest = new Quest("Empty_Quest", "Empty Quest", "Nothing", reward);
        quest.setCurrentStep(quest.getTotalStages());
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

    @Test
    void firesUpdateQuestLogEventOnRewardClaim() {
        GameEventBus eventBus = new GameEventBus();
        QuestYarnRegistry registry = new QuestYarnRegistry("tests/quests_test/index.json", "tests/quests_test");
        QuestRewardService questRewardService = new QuestRewardService(eventBus, registry);
        RewardSystem rewardSystem = new RewardSystem(eventBus, questRewardService);

        Entity player = new Entity();
        QuestLog questLog = new QuestLog();
        player.add(questLog);

        QuestReward reward = new QuestReward(0, List.of());
        Quest quest = new Quest("claim_update_test", "Claim Quest", "Claim", reward, 1);
        quest.setCurrentStep(quest.getTotalStages());
        questLog.add(quest);

        AtomicInteger updateCount = new AtomicInteger();
        eventBus.subscribe(UpdateQuestLogEvent.class, event -> updateCount.incrementAndGet());

        eventBus.fire(new QuestRewardEvent(player, "claim_update_test"));

        assertTrue(quest.isRewardClaimed());
        assertEquals(1, updateCount.get());

        rewardSystem.dispose();
    }

    @Test
    void giverTimingRewardsOnlyOnQuestRewardEvent() {
        GameEventBus eventBus = new GameEventBus();
        QuestYarnRegistry registry = new QuestYarnRegistry("tests/quests_test/index.json", "tests/quests_test");
        QuestRewardService questRewardService = new QuestRewardService(eventBus, registry);
        RewardSystem rewardSystem = new RewardSystem(eventBus, questRewardService);

        Entity player = new Entity();
        QuestLog questLog = new QuestLog();
        player.add(questLog);

        QuestReward reward = new QuestReward(12, List.of("ring"));
        Quest quest = new Quest("giver_reward_test", "Giver Quest", "Talk to the giver", reward, 1);
        quest.setCurrentStep(1);
        questLog.add(quest);

        eventBus.fire(new QuestRewardEvent(player, "giver_reward_test"));

        Wallet wallet = Wallet.MAPPER.get(player);
        assertNotNull(wallet);
        assertEquals(12, wallet.getMoney());
        assertTrue(quest.isRewardClaimed());

        rewardSystem.dispose();
    }
}
