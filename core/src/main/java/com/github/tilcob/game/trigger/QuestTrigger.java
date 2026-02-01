package com.github.tilcob.game.trigger;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.Quest;
import com.github.tilcob.game.event.AddQuestEvent;
import com.github.tilcob.game.event.GameEventBus;

public class QuestTrigger implements TriggerHandler{
    private final GameEventBus eventBus;

    public QuestTrigger(GameEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void onEnter(Entity trigger, Entity triggeringEntity) {
        Quest quest = Quest.MAPPER.get(trigger);
        if (quest == null || quest.getQuestId().isBlank()) return;
        eventBus.fire(new AddQuestEvent(triggeringEntity, quest.getQuestId()));
    }

    @Override
    public void onExit(Entity trigger, Entity triggeringEntity) {

    }
}
