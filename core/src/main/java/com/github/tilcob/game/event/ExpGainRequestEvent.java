package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record ExpGainRequestEvent(
    Entity entity,
    String source,
    String npcType,
    float expMultiplier,
    int baseXp
) {
}
