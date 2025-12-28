package com.github.tilcob.game.tiled;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.tilcob.game.assets.AssetManager;
import com.github.tilcob.game.assets.MapAsset;
import com.github.tilcob.game.config.Constants;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TiledManager {
    private final AssetManager assetManager;
    private final World world;
    private Consumer<TiledMap> mapChangeConsumer;
    private Consumer<TiledMapTileMapObject> loadObjectConsumer;
    private LoadTileConsumer loadTileConsumer;
    private BiConsumer<String, MapObject> loadTriggerConsumer;
    private TiledMap currentMap;

    public TiledManager(AssetManager assetManager, World world) {
        this.assetManager = assetManager;
        this.world = world;
        this.mapChangeConsumer = null;
        this.loadObjectConsumer = null;
        this.loadTileConsumer = null;
        this.currentMap = null;
        this.loadTileConsumer = null;
    }


    public TiledMap loadMap(MapAsset mapAsset){
        TiledMap tiledMap = assetManager.load(mapAsset);
        tiledMap.getProperties().put(Constants.MAP_ASSET, mapAsset);
        return tiledMap;
    }

    public void setMap(TiledMap map) {
        if (currentMap != null) {
            assetManager.unload(currentMap.getProperties().get(Constants.MAP_ASSET, MapAsset.class));

            Array<Body> bodies = new Array<>();
            world.getBodies(bodies);
            for (Body body : bodies) {
                if (Constants.ENVIRONMENT.equals(body.getUserData())) {
                    world.destroyBody(body);
                }
            }
        }
        currentMap = map;
        loadMapObjects(map);
        if (mapChangeConsumer != null) {
            mapChangeConsumer.accept(map);
        }
    }

    private void loadMapObjects(TiledMap tiledMap) {
        for (MapLayer layer : tiledMap.getLayers()) {
            if (Constants.OBJECT_LAYER.equals(layer.getName())) {
                loadObjectLayer(layer);
            } else if (layer instanceof TiledMapTileLayer tileLayer) {
                loadTileLayer(tileLayer);
            } else if (Constants.TRIGGER_LAYER.equals(layer.getName())) {
                loadTriggerLayer(layer);
            }
        }

        spawnMapBoundary(tiledMap);
    }

    private void loadTriggerLayer(MapLayer layer) {
        if (loadTriggerConsumer == null) return;

        for (MapObject object : layer.getObjects()) {
            if (object.getName() == null || object.getName().isBlank()) {
                throw new GdxRuntimeException("Trigger must have a name: " + object);
            }
            if (object instanceof RectangleMapObject rectMapObject) {
                loadTriggerConsumer.accept(object.getName(), rectMapObject);
            } else {
                throw new GdxRuntimeException("Unsupported trigger object: " + object);
            }
        }
    }

    private void spawnMapBoundary(TiledMap tiledMap) {
        int width = tiledMap.getProperties().get(Constants.MAP_WIDTH, 0, Integer.class);
        int height = tiledMap.getProperties().get(Constants.MAP_HEIGHT, 0, Integer.class);
        int tileWidth = tiledMap.getProperties().get(Constants.TILE_WIDTH, 0, Integer.class);
        int tileHeight = tiledMap.getProperties().get(Constants.TILE_HEIGHT, 0, Integer.class);

        float mapWidth = width * tileWidth * Constants.UNIT_SCALE;
        float mapHeight = height * tileHeight * Constants.UNIT_SCALE;
        float halfMapWidth = mapWidth *.5f;
        float halfMapHeight = mapHeight * .5f;

        float boxThickness = .5f;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.setZero();
        bodyDef.fixedRotation = true;
        Body body = world.createBody(bodyDef);
        body.setUserData(Constants.ENVIRONMENT);

        // left edge
        createBoundary(boxThickness, halfMapHeight, -boxThickness, halfMapHeight, body);

        // right edge
        createBoundary(boxThickness, halfMapHeight, mapWidth + boxThickness, halfMapHeight, body);

        // bottom edge
        createBoundary(halfMapWidth, boxThickness, halfMapWidth, -boxThickness, body);

        // top edge
        createBoundary(halfMapWidth, boxThickness, halfMapWidth, mapHeight + boxThickness, body);
    }

    private void createBoundary(float width, float height, float centerX, float centerY, Body body) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width, height, new Vector2(centerX, centerY), 0f);
        body.createFixture(shape, 0f).setFriction(0f);
        shape.dispose();
    }

    private void loadTileLayer(TiledMapTileLayer tileLayer) {
        if (loadObjectConsumer == null) return;

        for (int y = 0; y < tileLayer.getHeight(); y++) {
            for (int x = 0; x < tileLayer.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
                if (cell == null) continue;

                loadTileConsumer.accept(cell.getTile(), x, y);
            }
        }
    }

    private void loadObjectLayer(MapLayer objectLayer) {
        if (loadObjectConsumer == null) return;

        for (MapObject object : objectLayer.getObjects()) {
            if (object instanceof TiledMapTileMapObject tileMapObject) {
                loadObjectConsumer.accept(tileMapObject);
            } else {
                throw new GdxRuntimeException("Unsupported object type: " + object.getClass().getSimpleName());
            }
        }
    }

    public void setMapChangeConsumer(Consumer<TiledMap> mapChangeConsumer) {
        this.mapChangeConsumer = mapChangeConsumer;
    }

    public void setLoadObjectConsumer(Consumer<TiledMapTileMapObject> loadObjectConsumer) {
        this.loadObjectConsumer = loadObjectConsumer;
    }

    public void setLoadTileConsumer(LoadTileConsumer loadTileConsumer) {
        this.loadTileConsumer = loadTileConsumer;
    }

    public void setLoadTriggerConsumer(BiConsumer<String, MapObject> loadTriggerConsumer) {
        this.loadTriggerConsumer = loadTriggerConsumer;
    }

    @FunctionalInterface
    public interface LoadTileConsumer {
        void accept(TiledMapTile tile, float x, float y);


    }
}
