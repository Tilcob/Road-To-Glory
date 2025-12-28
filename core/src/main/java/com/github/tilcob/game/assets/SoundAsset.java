package com.github.tilcob.game.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Sound;

public enum SoundAsset implements Asset<Sound> {
    SWORD_HIT("sword_hit.wav"),
    LIFE_REG("life_reg.wav"),
    SWING("swing.wav"),
    TRAP("trap.wav");

    private final AssetDescriptor<Sound> descriptor;
    SoundAsset(String fileName) {
        this.descriptor = new AssetDescriptor<>("audio/" + fileName, Sound.class);
    }


    @Override
    public AssetDescriptor<Sound> getAssetDescriptor() {
        return descriptor;
    }
}
