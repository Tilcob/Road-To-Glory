package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record XPGainEvent(Entity entity, String treeId, int amount) {
}
