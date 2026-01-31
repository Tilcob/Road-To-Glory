package com.github.tilcob.game.trigger;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.Chest;
import com.github.tilcob.game.component.OpenChestRequest;
import com.github.tilcob.game.event.CloseChestEvent;
import com.github.tilcob.game.event.GameEventBus;

public class ChestTriggerHandler implements TriggerHandler {
    private final GameEventBus eventBus;

    public ChestTriggerHandler(GameEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void execute(Entity chest, Entity triggeringEntity) {
        OpenChestRequest openChestRequest = OpenChestRequest.MAPPER.get(triggeringEntity);

        if (openChestRequest == null || openChestRequest.getChest() != null) {
            triggeringEntity.add(new OpenChestRequest(chest));
        }
    }

    @Override
    public void exit(Entity trigger, Entity triggeringEntity) {
        eventBus.fire(new CloseChestEvent(triggeringEntity, Chest.MAPPER.get(trigger)));
        triggeringEntity.remove(OpenChestRequest.class);
    }
}
