package com.github.tilcob.game.trigger;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.OpenChestRequest;

public class ChestTriggerHandler implements TriggerHandler {

    @Override
    public void execute(Entity chest, Entity triggeringEntity) {
        triggeringEntity.add(new OpenChestRequest(chest));
    }
}
