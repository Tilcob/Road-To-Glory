package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.event.AddQuestEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.QuestCompletedEvent;
import com.github.tilcob.game.test.HeadlessGdxTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

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
}
