package com.github.tilcob.game.entity;

import com.badlogic.ashley.core.Entity;

public interface EntityLookup {
    Entity find(String name);

    Entity getPlayer();
}
