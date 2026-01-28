package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record RequestCompleteQuestEvent(Entity player, String questId) {
}
