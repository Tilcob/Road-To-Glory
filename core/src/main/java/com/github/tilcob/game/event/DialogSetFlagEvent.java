package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record DialogSetFlagEvent(Entity player, String flag, boolean value) {
}
