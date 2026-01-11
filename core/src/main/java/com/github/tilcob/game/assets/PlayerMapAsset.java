package com.github.tilcob.game.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.maps.tiled.BaseTiledMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;

public enum PlayerMapAsset implements Asset<TiledMap> {
    PLAYER("player.tmx"),;

    private final AssetDescriptor<TiledMap> descriptor;

    PlayerMapAsset(String mapName) {
        BaseTiledMapLoader.Parameters parameters = new BaseTiledMapLoader.Parameters();
        parameters.projectFilePath = "maps/Road-To-Glory.tiled-project";
        this.descriptor = new AssetDescriptor<>(mapName, TiledMap.class, parameters);
    }

    @Override
    public AssetDescriptor<TiledMap> getAssetDescriptor() {
        return descriptor;
    }
}
