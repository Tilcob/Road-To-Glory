package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record StartDialogEvent(Entity player, String npcId, String nodeId) {
}
