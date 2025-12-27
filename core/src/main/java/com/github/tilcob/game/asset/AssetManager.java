package com.github.tilcob.game.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.github.tommyettinger.freetypist.FreeTypistSkinLoader;

public class AssetManager implements Disposable {
    private final com.badlogic.gdx.assets.AssetManager assetManager;

    public AssetManager(FileHandleResolver resolver) {
        this.assetManager = new com.badlogic.gdx.assets.AssetManager(resolver);
        this.assetManager.setLoader(TiledMap.class, new TmxMapLoader());
        this.assetManager.setLoader(Skin.class, new FreeTypistSkinLoader(resolver));
    }

    public <T> T load(Asset<T> asset) {
        assetManager.load(asset.getDescriptor());
        assetManager.finishLoading();
        return assetManager.get(asset.getDescriptor());
    }

    public <T> void queue(Asset<T> asset) {
        assetManager.load(asset.getDescriptor());
    }

    public <T> T get(Asset<T> asset) {
        return assetManager.get(asset.getDescriptor());
    }

    public <T> void unload(Asset<T> asset) {
        assetManager.unload(asset.getDescriptor().fileName);
    }

    public boolean update() {
        // assetManager.getProgress(); value between 0 and 1
        return assetManager.update();
    }

    public void debugDiagnostics() {
        Gdx.app.debug("AssetManager", assetManager.getDiagnostics());
    }

    @Override
    public void dispose() {
        assetManager.dispose();
    }
}
