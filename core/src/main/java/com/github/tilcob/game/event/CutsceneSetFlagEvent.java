package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record CutsceneSetFlagEvent(Entity player, String flag, boolean value) {
}
