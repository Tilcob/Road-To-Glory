package com.github.tilcob.game.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.maps.tiled.BaseTiledMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;

public enum MapAsset implements Asset<TiledMap> {
    MAIN("main.tmx"),
    HOUSE("house.tmx"),
    PLAYER("player.tmx"),;

    private final AssetDescriptor<TiledMap> mapAssetDescriptor;

    MapAsset(String mapName) {
        BaseTiledMapLoader.Parameters parameters = new BaseTiledMapLoader.Parameters();
        parameters.projectFilePath = "maps/Road-To-Glory.tiled-project";
        this.mapAssetDescriptor = new AssetDescriptor<>("maps/" + mapName, TiledMap.class, parameters);
    }

    @Override
    public AssetDescriptor<TiledMap> getAssetDescriptor() {
        return this.mapAssetDescriptor;
    }
}
