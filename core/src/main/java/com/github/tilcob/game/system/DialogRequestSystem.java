package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.tilcob.game.component.Dialog;
import com.github.tilcob.game.component.StartDialogRequest;

public class DialogRequestSystem extends IteratingSystem {
    public DialogRequestSystem() {
        super(Family.all(StartDialogRequest.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        StartDialogRequest startDialogRequest = StartDialogRequest.MAPPER.get(entity);
        Entity npc = startDialogRequest.getNpc();

        Dialog.MAPPER.get(npc).setState(Dialog.State.REQUEST);
        entity.remove(StartDialogRequest.class);
    }
}
