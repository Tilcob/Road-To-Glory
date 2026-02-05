package com.github.tilcob.game.flow;

import com.badlogic.ashley.core.Entity;

public record FlowContext(Entity player, Entity npc) {
    public FlowContext(Entity player) {
        this(player, null);
    }
}
