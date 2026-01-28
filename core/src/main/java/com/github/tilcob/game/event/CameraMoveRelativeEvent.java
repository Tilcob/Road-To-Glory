package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record CameraMoveRelativeEvent(Entity player, float offsetX, float offsetY, float duration) {
}
