package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record LevelUpEvent(Entity entity, String treeId, int levelsGained, int newLevel) {
}
