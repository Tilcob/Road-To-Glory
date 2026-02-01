package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record SkillUnlockEvent(Entity entity, String treeId, String nodeId) {
}
