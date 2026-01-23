package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.Chest;

public record CloseChestEvent(Entity player, Chest chest) {
}
