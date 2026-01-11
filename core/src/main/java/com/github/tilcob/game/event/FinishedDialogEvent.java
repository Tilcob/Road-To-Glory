package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record FinishedDialogEvent(int messages, Entity entity) {
}
