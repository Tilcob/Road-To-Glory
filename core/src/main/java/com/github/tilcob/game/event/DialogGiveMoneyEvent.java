package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record DialogGiveMoneyEvent(Entity player, int amount) {
}
