package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.assets.MusicAsset;

public record PlayMusicEvent(Entity player, MusicAsset musicAsset) {
}
