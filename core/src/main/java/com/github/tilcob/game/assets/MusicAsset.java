package com.github.tilcob.game.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Music;

public enum MusicAsset implements Asset<Music> {
    TOWN("town.ogg"),
    MENU("menu.ogg");

    private final AssetDescriptor<Music> descriptor;

    MusicAsset(String fileName) {
        this.descriptor = new AssetDescriptor<>("audio/" + fileName, Music.class);
    }


    @Override
    public AssetDescriptor<Music> getAssetDescriptor() {
        return descriptor;
    }
}
