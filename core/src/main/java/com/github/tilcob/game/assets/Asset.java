package com.github.tilcob.game.assets;

import com.badlogic.gdx.assets.AssetDescriptor;

public interface Asset<T> {
    AssetDescriptor<T> getAssetDescriptor();
}
