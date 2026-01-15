package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.event.*;
import com.github.tilcob.game.quest.QuestReward;
import com.github.tilcob.game.test.HeadlessGdxTest;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.step.QuestStep;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestSystemTest extends HeadlessGdxTest {

    @Test
    void firesRewardEventWhenQuestCompletes() {
        GameEventBus eventBus = new GameEventBus();
        QuestSystem questSystem = new QuestSystem(eventBus);
        Engine engine = new Engine();
        engine.addSystem(questSystem);

        Entity player = new Entity();
        player.add(new QuestLog());
        engine.addEntity(player);

        Quest quest = new Quest("TestQuest", "Test Quest", "Test", new QuestReward(0, List.of()));
        quest.getSteps().add(new TestStep(true));
        QuestLog.MAPPER.get(player).add(quest);

        AtomicBoolean rewardFired = new AtomicBoolean(false);
        eventBus.subscribe(QuestRewardEvent.class, event -> rewardFired.set(true));

        engine.update(0f);

        assertTrue(rewardFired.get());
    }

    @Test
    void firesRewardEventForZeroStepQuestOnAdd() {
        GameEventBus eventBus = new GameEventBus();
        QuestSystem questSystem = new QuestSystem(eventBus);
        Engine engine = new Engine();
        engine.addSystem(questSystem);

        Entity player = new Entity();
        player.add(new QuestLog());
        engine.addEntity(player);

        AtomicBoolean rewardFired = new AtomicBoolean(false);
        eventBus.subscribe(QuestRewardEvent.class, event -> rewardFired.set(true));

        eventBus.fire(new AddQuestEvent(player, "Talk"));

        assertTrue(rewardFired.get());
    }

    private record TestStep(boolean completed) implements QuestStep {

        @Override
        public void start() { }

        @Override
        public void end() { }
    }
}
