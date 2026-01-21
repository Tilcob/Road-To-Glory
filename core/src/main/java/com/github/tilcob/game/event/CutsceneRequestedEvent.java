package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record CutsceneRequestedEvent(Entity player, String cutsceneId) {
}
