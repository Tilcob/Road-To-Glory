package com.github.tilcob.game.trigger;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.StartDialogRequest;

public class DialogTrigger implements TriggerHandler {
    @Override
    public void onEnter(Entity trigger, Entity triggeringEntity) {
        triggeringEntity.add(new StartDialogRequest(trigger));
    }

    @Override
    public void onExit(Entity trigger, Entity triggeringEntity) {
        triggeringEntity.remove(StartDialogRequest.class);
    }
}
