package com.github.tilcob.game.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.config.Constants;
import com.github.tommyettinger.freetypist.FreeTypistSkinLoader;

import java.util.Locale;

public class AssetManager implements Disposable {
    private final com.badlogic.gdx.assets.AssetManager assetManager;
    private TiledMap playerMap;

    public AssetManager(FileHandleResolver resolver) {
        this.assetManager = new com.badlogic.gdx.assets.AssetManager(resolver);
        this.assetManager.setLoader(TiledMap.class, new TmxMapLoader());
        this.assetManager.setLoader(Skin.class, new FreeTypistSkinLoader(resolver));
    }

    public <T> void queue(Asset<T> asset) {
        assetManager.load(asset.getAssetDescriptor());
    }

    public <T> T get(Asset<T> asset) {
        return assetManager.get(asset.getAssetDescriptor());
    }

    public <T> T loadSync(Asset<T> asset) {
        assetManager.load(asset.getAssetDescriptor());
        assetManager.finishLoading();
        return assetManager.get(asset.getAssetDescriptor());
    }

    public <T> void unload(Asset<T> asset) {
        assetManager.unload(asset.getAssetDescriptor().fileName);
    }

    public boolean update() {
        // assetManager.getProgress(); value between 0 and 1
        return assetManager.update();
    }

    public void finishLoading() {
        assetManager.finishLoading();
    }

    public TiledMapTile getPlayerTile() {
        if (playerMap == null) {
            playerMap = loadSync(MapAsset.PLAYER);
        }
        TiledMapTileSet objects = playerMap.getTileSets().getTileSet("objects");
        return objects.getTile(Constants.PLAYER_ID);
    }

    public void debugDiagnostics() {
        Gdx.app.debug("AssetManager", assetManager.getDiagnostics());
    }

    public boolean isLoaded(String internalPath) {
        return assetManager.isLoaded(internalPath);
    }

    public void reloadByPath(String internalPath) {
        if (internalPath == null || internalPath.isBlank()) return;

        String path = internalPath.replace("\\", "/");
        if (assetManager.isLoaded(path)) {
            assetManager.unload(path);
        }

        String lower = path.toLowerCase(Locale.ROOT);

        if (lower.endsWith(".atlas")) {
            assetManager.load(path, TextureAtlas.class);
            assetManager.finishLoadingAsset(path);
            return;
        }
        if (lower.endsWith(".json")) {
            assetManager.load(path, Skin.class);
            assetManager.finishLoadingAsset(path);
            return;
        }
        if (lower.endsWith(".tmx")) {
            assetManager.load(path, TiledMap.class);
            assetManager.finishLoadingAsset(path);
            return;
        }
        if (lower.endsWith(".wav") || lower.endsWith(".ogg") || lower.endsWith(".mp3")) {
            assetManager.load(path, Sound.class);
            assetManager.finishLoadingAsset(path);
            return;
        }

        Gdx.app.error("AssetManager", "Unknown asset type for reload: " + path);
    }

    public void resetCaches() {
        playerMap = null;
    }

    @Override
    public void dispose() {
        if (playerMap != null) {
            playerMap.dispose();
            playerMap = null;
        }
        assetManager.dispose();
    }
}
