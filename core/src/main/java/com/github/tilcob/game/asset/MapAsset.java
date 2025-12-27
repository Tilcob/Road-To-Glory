package com.github.tilcob.game.asset;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.maps.tiled.BaseTiledMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMap;

public enum MapAsset implements Asset<TiledMap> {
    MAIN("main.tmx");

    private final AssetDescriptor<TiledMap> descriptor;

    MapAsset(String path) {
        BaseTiledMapLoader.Parameters parameters = new BaseTiledMapLoader.Parameters();
        parameters.projectFilePath = "maps/Road-To-Glory.tiled-project";
        this.descriptor = new AssetDescriptor<>("maps/" + path, TiledMap.class);
    }


    @Override
    public AssetDescriptor<TiledMap> getDescriptor() {
        return descriptor;
    }
}
