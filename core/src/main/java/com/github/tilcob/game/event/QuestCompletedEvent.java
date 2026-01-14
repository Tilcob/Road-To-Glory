package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record QuestCompletedEvent(Entity player, String questId) {
}
