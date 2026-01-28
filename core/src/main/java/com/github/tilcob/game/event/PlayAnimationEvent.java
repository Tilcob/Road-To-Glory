package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.github.tilcob.game.component.Animation2D;

public record PlayAnimationEvent(Entity player,
                                 Entity target,
                                 Animation2D.AnimationType type,
                                 Animation.PlayMode playMode) {
}
