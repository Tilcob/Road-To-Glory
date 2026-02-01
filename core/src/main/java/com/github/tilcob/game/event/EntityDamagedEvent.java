package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record EntityDamagedEvent(Entity entity, float damage) {
}
