package com.github.tilcob.game.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.config.Constants;
import com.github.tommyettinger.freetypist.FreeTypistSkinLoader;

public class AssetManager implements Disposable {
    private final com.badlogic.gdx.assets.AssetManager assetManager;

    public AssetManager(FileHandleResolver resolver) {
        this.assetManager = new com.badlogic.gdx.assets.AssetManager(resolver);
        this.assetManager.setLoader(TiledMap.class, new TmxMapLoader());
        this.assetManager.setLoader(Skin.class, new FreeTypistSkinLoader(resolver));
    }

    public <T> T load(Asset<T> asset) {
        assetManager.load(asset.getAssetDescriptor());
        assetManager.finishLoading();
        return assetManager.get(asset.getAssetDescriptor());
    }

    public <T> void queue(Asset<T> asset) {
        assetManager.load(asset.getAssetDescriptor());
    }

    public <T> T get(Asset<T> asset) {
        return assetManager.get(asset.getAssetDescriptor());
    }

    public <T> void unload(Asset<T> asset) {
        assetManager.unload(asset.getAssetDescriptor().fileName);
    }

    public boolean update() {
        // assetManager.getProgress(); value between 0 and 1
        return assetManager.update();
    }

    public TiledMapTile getPlayerTile() {
        TiledMap player = new TmxMapLoader().load("maps/player.tmx");
        TiledMapTileSet objects = player.getTileSets().getTileSet("objects");
        return objects.getTile(Constants.PLAYER_ID);
    }

    public void debugDiagnostics() {
        Gdx.app.debug("AssetManager", assetManager.getDiagnostics());
    }

    @Override
    public void dispose() {
        assetManager.dispose();
    }
}
