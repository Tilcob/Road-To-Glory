package com.github.tilcob.game.entity;

import com.badlogic.ashley.core.Entity;

@FunctionalInterface
public interface EntityLookup {
    Entity find(String name);
}
