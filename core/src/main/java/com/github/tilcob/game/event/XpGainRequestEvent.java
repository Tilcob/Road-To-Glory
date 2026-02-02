package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record XpGainRequestEvent(
    Entity entity,
    String source,
    String npcType,
    float tileMultiplier,
    int baseXp
) {
}
