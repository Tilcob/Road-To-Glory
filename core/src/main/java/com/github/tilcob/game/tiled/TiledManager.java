package com.github.tilcob.game.tiled;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.tilcob.game.assets.AssetManager;
import com.github.tilcob.game.assets.MapAsset;
import com.github.tilcob.game.component.MapEntity;
import com.github.tilcob.game.component.Physic;
import com.github.tilcob.game.component.Trigger;
import com.github.tilcob.game.config.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TiledManager {
    private final AssetManager assetManager;
    private final World world;
    private Consumer<TiledMap> mapChangeConsumer;
    private Consumer<TiledMapTileMapObject> loadObjectConsumer;
    private LoadTileConsumer loadTileConsumer;
    private BiConsumer<Trigger.Type, MapObject> loadTriggerConsumer;
    private TiledMap currentMap;
    private final Engine engine;
    private final Map<MapAsset, Vector2> spawnPoints;

    public TiledManager(AssetManager assetManager, World world, Engine engine) {
        this.assetManager = assetManager;
        this.world = world;
        this.mapChangeConsumer = null;
        this.loadObjectConsumer = null;
        this.loadTileConsumer = null;
        this.currentMap = null;
        this.loadTileConsumer = null;
        this.engine = engine;
        this.spawnPoints = new HashMap<>();
    }


    public TiledMap loadMap(MapAsset mapAsset){
        TiledMap tiledMap = assetManager.loadSync(mapAsset);
        tiledMap.getProperties().put(Constants.MAP_ASSET, mapAsset);
        return tiledMap;
    }

    public MapAsset getCurrentMapAsset() {
        return currentMap.getProperties().get(Constants.MAP_ASSET, MapAsset.class);
    }

    public void setMap(TiledMap map) {
        clearMapEntities(engine);
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

    public void clearMapEntities(Engine engine) {
        ImmutableArray<Entity> entities = engine.getEntitiesFor(Family.all(MapEntity.class).get());
        com.badlogic.gdx.utils.Array<Entity> snapshot = new com.badlogic.gdx.utils.Array<>(entities.size());
        for (int i = 0; i < entities.size(); i++) {
            snapshot.add(entities.get(i));
        }

        for (Entity e : snapshot) {
            engine.removeEntity(e);
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
            String classType = object.getProperties().get(Constants.TYPE, "", String.class);
            if (!classType.equals(Constants.TRIGGER_CLASS)) throw new GdxRuntimeException("Trigger must have the Trigger class: " + object);
            if (object instanceof RectangleMapObject
                || object instanceof EllipseMapObject
                || object instanceof PolygonMapObject
                || object instanceof PolylineMapObject) {
                String typeStr = object.getProperties().get(Constants.TRIGGER_TYPE, "", String.class);
                loadTriggerConsumer.accept(Trigger.Type.valueOf(typeStr), object);
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
            } else if (object instanceof RectangleMapObject rectObject) {
                String type = rectObject.getProperties().get(Constants.TYPE, "", String.class);
                if (type.equals(Constants.SPAWN_CLASS)) {
                    Rectangle rect = rectObject.getRectangle();
                    Vector2 spawnPoint = new Vector2(rect.x * Constants.UNIT_SCALE, rect.y * Constants.UNIT_SCALE);
                    spawnPoints.put(getCurrentMapAsset(), spawnPoint);
                }
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

    public void setLoadTriggerConsumer(BiConsumer<Trigger.Type, MapObject> loadTriggerConsumer) {
        this.loadTriggerConsumer = loadTriggerConsumer;
    }

    public TiledMap getCurrentMap() {
        return currentMap;
    }

    public Map<MapAsset, Vector2> getSpawnPoints() {
        return spawnPoints;
    }

    public Vector2 getSpawnPoint() {
        return spawnPoints.get(getCurrentMapAsset());
    }

    @FunctionalInterface
    public interface LoadTileConsumer {
        void accept(TiledMapTile tile, float x, float y);

    }
}
