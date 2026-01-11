package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record ExitTriggerEvent(Entity player, Entity trigger) {
}
