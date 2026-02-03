package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record ExpGainEvent(Entity entity, String treeId, int amount) {
}
