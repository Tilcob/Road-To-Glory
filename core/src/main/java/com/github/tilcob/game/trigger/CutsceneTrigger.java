package com.github.tilcob.game.trigger;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.CutsceneReference;
import com.github.tilcob.game.event.CutsceneRequestedEvent;
import com.github.tilcob.game.event.GameEventBus;

public class CutsceneTrigger implements TriggerHandler {
    private final GameEventBus eventBus;

    public CutsceneTrigger(GameEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void onEnter(Entity trigger, Entity triggeringEntity) {
        CutsceneReference reference = CutsceneReference.MAPPER.get(trigger);
        if (reference == null) return;
        eventBus.fire(new CutsceneRequestedEvent(triggeringEntity, reference.getCutsceneId()));
    }

    @Override
    public void onExit(Entity trigger, Entity triggeringEntity) {
    }
}
