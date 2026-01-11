package com.github.tilcob.game.trigger;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.Dialog;
import com.github.tilcob.game.component.StartDialogRequest;

public class DialogTrigger implements TriggerHandler {
    @Override
    public void execute(Entity trigger, Entity triggeringEntity) {
        triggeringEntity.add(new StartDialogRequest(trigger));
    }

    @Override
    public void exit(Entity trigger, Entity triggeringEntity) {
        triggeringEntity.remove(StartDialogRequest.class);
    }
}
