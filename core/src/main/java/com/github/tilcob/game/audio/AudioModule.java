package com.github.tilcob.game.audio;

import com.github.tilcob.game.assets.AssetManager;

public class AudioModule {
    private AudioModule() {
    }

    public static AudioManager create(AssetManager assetManager) {
        return new AudioManager(assetManager);
    }
}
