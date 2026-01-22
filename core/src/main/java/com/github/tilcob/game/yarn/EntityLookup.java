package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;

@FunctionalInterface
public interface EntityLookup {
    Entity find(Entity player, String entityId);
}
