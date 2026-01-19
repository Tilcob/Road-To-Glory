package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.Npc;
import com.github.tilcob.game.dialog.DialogData;
import com.github.tilcob.game.dialog.QuestDialog;
import com.github.tilcob.game.event.DialogFinishedEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.QuestCompletedEvent;
import com.github.tilcob.game.event.QuestRewardEvent;
import com.github.tilcob.game.quest.QuestYarnRegistry;
import com.github.tilcob.game.test.HeadlessGdxTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class QuestRewardSchedulerSystemTest extends HeadlessGdxTest {

    @Test
    void firesRewardForCompletionTimingOnQuestCompletedEvent() {
        GameEventBus eventBus = new GameEventBus();
        QuestYarnRegistry registry = new QuestYarnRegistry("tests/quests_test/index.json", "tests/quests_test");
        QuestRewardSchedulerSystem scheduler = new QuestRewardSchedulerSystem(eventBus, registry, Map.of());

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
        QuestRewardSchedulerSystem scheduler = new QuestRewardSchedulerSystem(eventBus, registry, Map.of());

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
        QuestRewardSchedulerSystem scheduler = new QuestRewardSchedulerSystem(eventBus, registry, Map.of());

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
        QuestRewardSchedulerSystem scheduler = new QuestRewardSchedulerSystem(eventBus, registry, dialogs);

        Entity npc = new Entity();
        npc.add(new Npc(null, "QuestGiver"));
        Entity player = new Entity();
        AtomicReference<QuestRewardEvent> rewardEvent = new AtomicReference<>();
        eventBus.subscribe(QuestRewardEvent.class, rewardEvent::set);

        eventBus.fire(new DialogFinishedEvent(npc, player));

        assertNotNull(rewardEvent.get());
        assertEquals("giver_reward_test", rewardEvent.get().questId());

        scheduler.dispose();
    }
}
