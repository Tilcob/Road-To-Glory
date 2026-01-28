package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.Facing;

public record FaceEntityEvent(Entity player, Entity target, Facing.FacingDirection direction) {
}
