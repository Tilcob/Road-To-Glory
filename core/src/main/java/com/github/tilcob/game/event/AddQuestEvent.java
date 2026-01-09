package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record AddQuestEvent(Entity player, String questId) {
}
