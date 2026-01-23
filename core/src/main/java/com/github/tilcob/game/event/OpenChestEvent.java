package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record OpenChestEvent(Entity player, Entity chestEntity) {
}
