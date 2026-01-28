package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record MoveToEvent(Entity player, Entity target, float x, float y, float arrivalDistance) {

    public MoveToEvent(Entity player, Entity target, float x, float y) {
        this(player, target, x, y, 0.1f);
    }
}
