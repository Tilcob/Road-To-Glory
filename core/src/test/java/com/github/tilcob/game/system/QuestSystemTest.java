package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.event.AddQuestEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.QuestCompletedEvent;
import com.github.tilcob.game.event.UpdateQuestLogEvent;
import com.github.tilcob.game.test.HeadlessGdxTest;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.step.QuestStep;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestSystemTest extends HeadlessGdxTest {

    @Test
    void firesCompletionEventForZeroStepQuestOnAdd() {
        GameEventBus eventBus = new GameEventBus();
        QuestSystem questSystem = new QuestSystem(eventBus);

        Entity player = new Entity();
        player.add(new QuestLog());

        AtomicBoolean completed = new AtomicBoolean(false);
        eventBus.subscribe(QuestCompletedEvent.class, event -> completed.set(true));

        eventBus.fire(new AddQuestEvent(player, "Talk"));

        assertTrue(completed.get());

        questSystem.dispose();
    }

    @Test
    void advancesQuestStepsAndEmitsUpdateEvents() {
        GameEventBus eventBus = new GameEventBus();
        QuestSystem questSystem = new QuestSystem(eventBus);

        Entity player = new Entity();
        QuestLog questLog = new QuestLog();
        player.add(questLog);

        Quest quest = new Quest("Step_Quest", "Step Quest", "Has steps");
        quest.getSteps().add(new CompletedStep());
        questLog.add(quest);

        AtomicInteger updateEvents = new AtomicInteger(0);
        AtomicBoolean completed = new AtomicBoolean(false);
        eventBus.subscribe(UpdateQuestLogEvent.class, event -> updateEvents.incrementAndGet());
        eventBus.subscribe(QuestCompletedEvent.class, event -> completed.set(true));

        questSystem.processEntity(player, 0f);

        assertEquals(1, quest.getCurrentStep());
        assertEquals(1, updateEvents.get());
        assertTrue(completed.get());

        questSystem.dispose();
    }

    private static final class CompletedStep implements QuestStep {
        @Override
        public boolean isCompleted() {
            return true;
        }

        @Override
        public void start() {
        }

        @Override
        public void end() {
        }
    }
}
