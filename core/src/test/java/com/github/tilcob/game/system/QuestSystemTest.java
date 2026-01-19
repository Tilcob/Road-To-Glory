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
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.QuestReward;
import com.github.tilcob.game.quest.QuestYarnRegistry;
import com.github.tilcob.game.test.HeadlessGdxTest;
import com.github.tilcob.game.yarn.QuestYarnBridge;
import com.github.tilcob.game.yarn.QuestYarnRuntime;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestSystemTest extends HeadlessGdxTest {

    @Test
    void firesRewardEventWhenQuestCompletes() {
        GameEventBus eventBus = new GameEventBus();
        QuestYarnRegistry registry = new QuestYarnRegistry("tests/quests_test/index.json", "tests/quests_test");
        QuestSystem questSystem = new QuestSystem(
            eventBus,
            registry,
            new QuestYarnRuntime(new QuestYarnBridge(eventBus, true), Map.of(), Map.of())
        );
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
        QuestSystem questSystem = new QuestSystem(
            eventBus,
            registry,
            new QuestYarnRuntime(new QuestYarnBridge(eventBus, true), Map.of(), Map.of())
        );
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
        Map<String, DialogData> questDialogs = Map.of("start_node_flag_test", dialogData);
        QuestSystem questSystem = new QuestSystem(
            eventBus,
            registry,
            new QuestYarnRuntime(new QuestYarnBridge(eventBus, true), Map.of(), questDialogs)
        );
        Engine engine = new Engine();
        engine.addSystem(questSystem);

        Entity player = new Entity();
        player.add(new QuestLog());
        engine.addEntity(player);

        eventBus.fire(new AddQuestEvent(player, "start_node_flag_test"));

        DialogFlags flags = DialogFlags.MAPPER.get(player);
        assertTrue(flags != null && flags.get("start_node_flag_test"));
    }
}
