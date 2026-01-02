package com.github.tilcob.game.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public enum SkinAsset implements Asset<Skin> {
    DEFAULT("skin.json"),;

    private final AssetDescriptor<Skin> descriptor;
    SkinAsset(String skinFile) {
        this.descriptor = new AssetDescriptor<>("ui/" + skinFile, Skin.class);
    }

    @Override
    public AssetDescriptor<Skin> getAssetDescriptor() {
        return descriptor;
    }
}
