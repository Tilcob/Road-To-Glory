package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record SetFlagEvent(Entity player, String flag, boolean value) {
}
