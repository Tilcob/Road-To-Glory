package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record QuestIncCounterEvent(Entity player, String counter, int delta) {
}
