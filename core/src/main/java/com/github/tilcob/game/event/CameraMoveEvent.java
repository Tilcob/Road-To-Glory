package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record CameraMoveEvent(Entity player, float x, float y, float duration) {
}
