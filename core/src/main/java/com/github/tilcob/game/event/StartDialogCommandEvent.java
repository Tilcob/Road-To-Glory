package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record StartDialogCommandEvent(Entity player, String npcId, String nodeId) {
}
