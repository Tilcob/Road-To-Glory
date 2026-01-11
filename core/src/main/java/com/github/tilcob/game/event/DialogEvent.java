package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;

public record DialogEvent(Array<String> lines, Entity entity) {
}
