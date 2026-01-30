package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.github.tilcob.game.component.DialogFlags;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.dialog.DialogData;
import com.github.tilcob.game.dialog.YarnDialogLoader;
import com.github.tilcob.game.event.AddQuestEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.QuestCompletedEvent;
import com.github.tilcob.game.event.UpdateQuestLogEvent;
import com.github.tilcob.game.flow.FlowBootstrap;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.QuestLifecycleService;
import com.github.tilcob.game.quest.QuestReward;
import com.github.tilcob.game.quest.QuestYarnRegistry;
import com.github.tilcob.game.test.HeadlessGdxTest;
import com.github.tilcob.game.yarn.QuestYarnRuntime;
import com.github.tilcob.game.yarn.YarnRuntime;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class QuestSystemTest extends HeadlessGdxTest {

    @Test
    void firesRewardEventWhenQuestCompletes() {
        GameEventBus eventBus = new GameEventBus();
        QuestYarnRegistry registry = new QuestYarnRegistry("tests/quests_test/index.json", "tests/quests_test");
        QuestLifecycleService questLifecycleService = new QuestLifecycleService(eventBus, registry, Map.of());

        QuestYarnRuntime questYarnRuntime = createQuestRuntime(eventBus, questLifecycleService, registry);
        questLifecycleService.setQuestYarnRuntime(questYarnRuntime);

        QuestSystem questSystem = new QuestSystem(eventBus, questLifecycleService);
        Engine engine = new Engine();
        engine.addSystem(questSystem);

        Entity player = new Entity();
        player.add(new QuestLog());
        engine.addEntity(player);

        Quest quest = new Quest("TestQuest", "Test Quest", "Test", new QuestReward(0, List.of()), 1);
        quest.setCurrentStep(1);
        QuestLog.MAPPER.get(player).add(quest);

        AtomicBoolean rewardFired = new AtomicBoolean(false);
        eventBus.subscribe(QuestCompletedEvent.class, event -> rewardFired.set(true));

        engine.update(0f);

        assertTrue(rewardFired.get());
    }

    @Test
    void firesRewardEventForZeroStepQuestOnAdd() {
        GameEventBus eventBus = new GameEventBus();
        QuestYarnRegistry registry = new QuestYarnRegistry("tests/quests_test/index.json", "tests/quests_test");
        QuestLifecycleService questLifecycleService = new QuestLifecycleService(eventBus, registry, Map.of());

        QuestYarnRuntime questYarnRuntime = createQuestRuntime(eventBus, questLifecycleService, registry);
        questLifecycleService.setQuestYarnRuntime(questYarnRuntime);

        QuestSystem questSystem = new QuestSystem(eventBus, questLifecycleService);
        Engine engine = new Engine();
        engine.addSystem(questSystem);

        Entity player = new Entity();
        player.add(new QuestLog());
        engine.addEntity(player);

        AtomicBoolean completedFired = new AtomicBoolean(false);
        eventBus.subscribe(QuestCompletedEvent.class, event -> completedFired.set(true));

        eventBus.fire(new AddQuestEvent(player, "zero_step_test"));

        assertTrue(completedFired.get());
    }

    @Test
    void executesStartNodeCommandsOnAdd() {
        GameEventBus eventBus = new GameEventBus();
        QuestYarnRegistry registry = new QuestYarnRegistry("tests/quests_test/index.json", "tests/quests_test");

        YarnDialogLoader dialogLoader = new YarnDialogLoader();
        DialogData dialogData = dialogLoader.load(Gdx.files.internal("tests/quests_test/start_node_flag_test.yarn"));

        // Wichtig: questDialogs muss wirklich an die Runtime übergeben werden
        Map<String, DialogData> questDialogs = Map.of("start_node_flag_test", dialogData);

        QuestLifecycleService questLifecycleService = new QuestLifecycleService(eventBus, registry, Map.of());

        QuestYarnRuntime questYarnRuntime = createQuestRuntime(eventBus, questLifecycleService, registry);
        questLifecycleService.setQuestYarnRuntime(questYarnRuntime);

        QuestSystem questSystem = new QuestSystem(eventBus, questLifecycleService);
        Engine engine = new Engine();
        engine.addSystem(questSystem);

        Entity player = new Entity();
        player.add(new QuestLog());
        engine.addEntity(player);

        eventBus.fire(new AddQuestEvent(player, "start_node_flag_test"));

        DialogFlags flags = DialogFlags.MAPPER.get(player);
        assertNotNull(flags);
        assertTrue(flags.get("start_node_flag_test"));
    }

    @Test
    void firesUpdateQuestLogEventOnStageChange() {
        GameEventBus eventBus = new GameEventBus();
        QuestYarnRegistry registry = new QuestYarnRegistry("tests/quests_test/index.json", "tests/quests_test");
        QuestLifecycleService questLifecycleService = new QuestLifecycleService(eventBus, registry, Map.of());

        QuestYarnRuntime questYarnRuntime = createQuestRuntime(eventBus, questLifecycleService, registry);
        questLifecycleService.setQuestYarnRuntime(questYarnRuntime);

        Entity player = new Entity();
        QuestLog questLog = new QuestLog();
        player.add(questLog);

        Quest quest = new Quest("stage_test", "Stage Quest", "Stage", new QuestReward(0, List.of()), 2);
        questLog.add(quest);

        AtomicInteger updateCount = new AtomicInteger();
        eventBus.subscribe(UpdateQuestLogEvent.class, event -> updateCount.incrementAndGet());

        questYarnRuntime.executeCommandLine(player, "<<quest_stage stage_test 1>>");

        assertEquals(1, quest.getCurrentStep());
        assertEquals(1, updateCount.get());
    }

    @Test
    void firesUpdateQuestLogEventOnCompletion() {
        GameEventBus eventBus = new GameEventBus();
        QuestYarnRegistry registry = new QuestYarnRegistry("tests/quests_test/index.json", "tests/quests_test");
        QuestLifecycleService questLifecycleService = new QuestLifecycleService(eventBus, registry, Map.of());

        QuestYarnRuntime questYarnRuntime = createQuestRuntime(eventBus, questLifecycleService, registry);
        questLifecycleService.setQuestYarnRuntime(questYarnRuntime);

        Entity player = new Entity();
        QuestLog questLog = new QuestLog();
        player.add(questLog);

        Quest quest = new Quest("complete_test", "Complete Quest", "Complete", new QuestReward(0, List.of()), 1);
        questLog.add(quest);

        AtomicInteger updateCount = new AtomicInteger();
        AtomicBoolean completed = new AtomicBoolean(false);
        eventBus.subscribe(UpdateQuestLogEvent.class, event -> updateCount.incrementAndGet());
        eventBus.subscribe(QuestCompletedEvent.class, event -> completed.set(true));

        questYarnRuntime.executeCommandLine(player, "<<quest_complete complete_test>>");

        assertTrue(quest.isCompleted());
        assertTrue(quest.isCompletionNotified());
        assertTrue(completed.get());
        assertEquals(1, updateCount.get());
    }

    private static QuestYarnRuntime createQuestRuntime(GameEventBus eventBus,
                                                       QuestLifecycleService questLifecycleService,
                                                       QuestYarnRegistry questYarnRegistry) {

        YarnRuntime yarn = new YarnRuntime();
        FlowBootstrap flowBootstrap = FlowBootstrap.create(eventBus, questLifecycleService,
            null, () -> null);

        return new QuestYarnRuntime(
            yarn, questYarnRegistry, flowBootstrap.commands(),
            flowBootstrap.executor(), flowBootstrap.functions());
    }
}
