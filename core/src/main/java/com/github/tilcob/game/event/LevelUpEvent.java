package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record LevelUpEvent(Entity entity, int levelsGained, int newLevel) {
}
