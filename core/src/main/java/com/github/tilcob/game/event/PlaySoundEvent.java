package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.assets.SoundAsset;

public record PlaySoundEvent(Entity player, SoundAsset soundAsset) {
}
