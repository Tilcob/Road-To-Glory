package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.dialog.DialogLine;

public record DialogEvent(DialogLine line, Entity entity) {
}
