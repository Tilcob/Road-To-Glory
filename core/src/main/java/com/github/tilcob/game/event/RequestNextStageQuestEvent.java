package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record RequestNextStageQuestEvent(Entity player, String questId, int stage) {
}
