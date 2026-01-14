package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.dialog.DialogChoice;

public record DialogChoiceResolvedEvent(Entity player, Entity npc, DialogChoice choice) {
}
