package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record UpdateChestInventoryEvent(Entity player, Entity chestEntity) {
}
