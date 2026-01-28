package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record FadeOutEvent(Entity player, float duration, float alpha) {
    public FadeOutEvent(Entity player, float duration) {
        this(player, duration, 1f);
    }
}
