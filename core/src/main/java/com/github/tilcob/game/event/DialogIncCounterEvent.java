package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record DialogIncCounterEvent(Entity player, String counter, int delta) {
}
