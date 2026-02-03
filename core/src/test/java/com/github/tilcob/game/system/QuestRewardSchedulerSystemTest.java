package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.Npc;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.component.Wallet;
import com.github.tilcob.game.dialog.DialogData;
import com.github.tilcob.game.dialog.QuestDialog;
import com.github.tilcob.game.event.*;
import com.github.tilcob.game.quest.*;
import com.github.tilcob.game.test.HeadlessGdxTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class QuestRewardSchedulerSystemTest extends HeadlessGdxTest {

    @Test
    void firesRewardForCompletionTimingOnQuestCompletedEvent() {
        GameEventBus eventBus = new GameEventBus();
        QuestYarnRegistry registry = new QuestYarnRegistry("tests/quests_test/index.json", "tests/quests_test");
        QuestLifecycleService questLifecycleService = new QuestLifecycleService(eventBus, registry, Map.of());
        QuestRewardSchedulerSystem scheduler = new QuestRewardSchedulerSystem(eventBus, questLifecycleService);

        Entity player = new Entity();
        AtomicReference<QuestRewardEvent> rewardEvent = new AtomicReference<>();
        eventBus.subscribe(QuestRewardEvent.class, rewardEvent::set);

        eventBus.fire(new QuestCompletedEvent(player, "completion_reward_test"));

        assertNotNull(rewardEvent.get());
        assertEquals(player, rewardEvent.get().player());
        assertEquals("completion_reward_test", rewardEvent.get().questId());

        scheduler.dispose();
    }

    @Test
    void firesRewardForAutoTimingOnQuestCompletedEvent() {
        GameEventBus eventBus = new GameEventBus();
        QuestYarnRegistry registry = new QuestYarnRegistry("tests/quests_test/index.json", "tests/quests_test");
        QuestLifecycleService questLifecycleService = new QuestLifecycleService(eventBus, registry, Map.of());
        QuestRewardSchedulerSystem scheduler = new QuestRewardSchedulerSystem(eventBus, questLifecycleService);

        Entity player = new Entity();
        AtomicReference<QuestRewardEvent> rewardEvent = new AtomicReference<>();
        eventBus.subscribe(QuestRewardEvent.class, rewardEvent::set);

        eventBus.fire(new QuestCompletedEvent(player, "auto_reward_test"));

        assertNotNull(rewardEvent.get());
        assertEquals("auto_reward_test", rewardEvent.get().questId());

        scheduler.dispose();
    }

    @Test
    void doesNotFireRewardForGiverTimingOnQuestCompletedEvent() {
        GameEventBus eventBus = new GameEventBus();
        QuestYarnRegistry registry = new QuestYarnRegistry("tests/quests_test/index.json", "tests/quests_test");
        QuestLifecycleService questLifecycleService = new QuestLifecycleService(eventBus, registry, Map.of());
        QuestRewardSchedulerSystem scheduler = new QuestRewardSchedulerSystem(eventBus, questLifecycleService);

        Entity player = new Entity();
        AtomicReference<QuestRewardEvent> rewardEvent = new AtomicReference<>();
        eventBus.subscribe(QuestRewardEvent.class, rewardEvent::set);

        eventBus.fire(new QuestCompletedEvent(player, "giver_reward_test"));

        assertNull(rewardEvent.get());

        scheduler.dispose();
    }

    @Test
    void firesRewardForGiverTimingOnDialogFinishedEvent() {
        GameEventBus eventBus = new GameEventBus();
        QuestYarnRegistry registry = new QuestYarnRegistry("tests/quests_test/index.json", "tests/quests_test");
        Map<String, DialogData> dialogs = new HashMap<>();
        QuestDialog questDialog = new QuestDialog("giver_reward_test", null, null, null);
        dialogs.put("QuestGiver", new DialogData(null, null, null, null, questDialog, null, null));
        QuestLifecycleService questLifecycleService = new QuestLifecycleService(eventBus, registry, dialogs);
        QuestRewardSchedulerSystem scheduler = new QuestRewardSchedulerSystem(eventBus, questLifecycleService);

        Entity npc = new Entity();
        npc.add(new Npc(null, "QuestGiver"));
        Entity player = new Entity();
        QuestLog questLog = new QuestLog();
        player.add(questLog);
        QuestReward reward = new QuestReward(0, 0, List.of());
        Quest quest = new Quest("giver_reward_test", "Giver Reward", "Reward after dialog", reward, 1);
        quest.setCurrentStep(quest.getTotalStages());
        questLog.add(quest);
        AtomicReference<QuestRewardEvent> rewardEvent = new AtomicReference<>();
        eventBus.subscribe(QuestRewardEvent.class, rewardEvent::set);

        eventBus.fire(new DialogFinishedEvent(npc, player));

        assertNotNull(rewardEvent.get());
        assertEquals("giver_reward_test", rewardEvent.get().questId());

        scheduler.dispose();
    }

    @Test
    void giverTimingRewardsOnlyAfterDialogAndNotTwice() {
        GameEventBus eventBus = new GameEventBus();
        QuestYarnRegistry registry = new QuestYarnRegistry("tests/quests_test/index.json", "tests/quests_test");
        Map<String, DialogData> dialogs = new HashMap<>();
        QuestDialog questDialog = new QuestDialog("giver_reward_test", null, null, null);
        dialogs.put("QuestGiver", new DialogData(null, null, null, null, questDialog, null, null));
        QuestLifecycleService questLifecycleService = new QuestLifecycleService(eventBus, registry, dialogs);
        QuestRewardSchedulerSystem scheduler = new QuestRewardSchedulerSystem(eventBus, questLifecycleService);
        QuestRewardService questRewardService = new QuestRewardService(eventBus, registry);
        RewardSystem rewardSystem = new RewardSystem(eventBus, questRewardService);

        Entity npc = new Entity();
        npc.add(new Npc(null, "QuestGiver"));
        Entity player = new Entity();
        QuestLog questLog = new QuestLog();
        player.add(questLog);
        QuestReward reward = new QuestReward(12, 0, List.of());
        Quest quest = new Quest("giver_reward_test", "Giver Reward", "Reward after dialog", reward, 1);
        quest.setCurrentStep(quest.getTotalStages());
        questLog.add(quest);

        AtomicInteger rewardGrantedCount = new AtomicInteger();
        eventBus.subscribe(RewardGrantedEvent.class, event -> rewardGrantedCount.incrementAndGet());

        eventBus.fire(new QuestCompletedEvent(player, "giver_reward_test"));

        assertEquals(0, rewardGrantedCount.get());
        assertFalse(quest.isRewardClaimed());

        eventBus.fire(new DialogFinishedEvent(npc, player));

        Wallet wallet = Wallet.MAPPER.get(player);
        assertNotNull(wallet);
        assertEquals(12, wallet.getMoney());
        assertTrue(quest.isRewardClaimed());
        assertEquals(1, rewardGrantedCount.get());

        eventBus.fire(new DialogFinishedEvent(npc, player));

        assertEquals(12, wallet.getMoney());
        assertEquals(1, rewardGrantedCount.get());

        rewardSystem.dispose();
        scheduler.dispose();
    }

    @Test
    void completionTimingRewardsOnQuestCompletedEventOnce() {
        GameEventBus eventBus = new GameEventBus();
        QuestYarnRegistry registry = new QuestYarnRegistry("tests/quests_test/index.json", "tests/quests_test");
        QuestLifecycleService questLifecycleService = new QuestLifecycleService(eventBus, registry, Map.of());
        QuestRewardSchedulerSystem scheduler = new QuestRewardSchedulerSystem(eventBus, questLifecycleService);
        QuestRewardService questRewardService = new QuestRewardService(eventBus, registry);
        RewardSystem rewardSystem = new RewardSystem(eventBus, questRewardService);

        Entity player = new Entity();
        QuestLog questLog = new QuestLog();
        player.add(questLog);
        QuestReward reward = new QuestReward(8, 0, List.of());
        Quest quest = new Quest("completion_reward_test", "Completion Reward", "Reward on completion", reward, 1);
        quest.setCurrentStep(quest.getTotalStages());
        questLog.add(quest);

        AtomicInteger rewardGrantedCount = new AtomicInteger();
        eventBus.subscribe(RewardGrantedEvent.class, event -> rewardGrantedCount.incrementAndGet());

        eventBus.fire(new QuestCompletedEvent(player, "completion_reward_test"));

        Wallet wallet = Wallet.MAPPER.get(player);
        assertNotNull(wallet);
        assertEquals(8, wallet.getMoney());
        assertTrue(quest.isRewardClaimed());
        assertEquals(1, rewardGrantedCount.get());

        eventBus.fire(new QuestCompletedEvent(player, "completion_reward_test"));

        assertEquals(8, wallet.getMoney());
        assertEquals(1, rewardGrantedCount.get());

        rewardSystem.dispose();
        scheduler.dispose();
    }

    @Test
    void autoTimingRewardsOnQuestCompletedEventOnceWithoutDialog() {
        GameEventBus eventBus = new GameEventBus();
        QuestYarnRegistry registry = new QuestYarnRegistry("tests/quests_test/index.json", "tests/quests_test");
        QuestLifecycleService questLifecycleService = new QuestLifecycleService(eventBus, registry, Map.of());
        QuestRewardSchedulerSystem scheduler = new QuestRewardSchedulerSystem(eventBus, questLifecycleService);
        QuestRewardService questRewardService = new QuestRewardService(eventBus, registry);
        RewardSystem rewardSystem = new RewardSystem(eventBus, questRewardService);

        Entity player = new Entity();
        QuestLog questLog = new QuestLog();
        player.add(questLog);
        QuestReward reward = new QuestReward(6, 0, List.of());
        Quest quest = new Quest("auto_reward_test", "Auto Reward", "Reward on completion", reward, 1);
        quest.setCurrentStep(quest.getTotalStages());
        questLog.add(quest);

        AtomicInteger rewardGrantedCount = new AtomicInteger();
        eventBus.subscribe(RewardGrantedEvent.class, event -> rewardGrantedCount.incrementAndGet());

        eventBus.fire(new QuestCompletedEvent(player, "auto_reward_test"));

        Wallet wallet = Wallet.MAPPER.get(player);
        assertNotNull(wallet);
        assertEquals(6, wallet.getMoney());
        assertTrue(quest.isRewardClaimed());
        assertEquals(1, rewardGrantedCount.get());

        eventBus.fire(new QuestCompletedEvent(player, "auto_reward_test"));

        assertEquals(6, wallet.getMoney());
        assertEquals(1, rewardGrantedCount.get());

        rewardSystem.dispose();
        scheduler.dispose();
    }
}
