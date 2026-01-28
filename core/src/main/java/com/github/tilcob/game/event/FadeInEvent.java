package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record FadeInEvent(Entity player, float duration, float alpha) {
    public FadeInEvent(Entity player, float duration) {
        this(player, duration, 0f);
    }
}
