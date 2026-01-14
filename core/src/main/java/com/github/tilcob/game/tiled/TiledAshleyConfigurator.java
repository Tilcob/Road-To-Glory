package com.github.tilcob.game.tiled;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.tilcob.game.assets.AssetManager;
import com.github.tilcob.game.assets.AtlasAsset;
import com.github.tilcob.game.assets.MapAsset;
import com.github.tilcob.game.assets.SoundAsset;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.item.ItemType;
import com.github.tilcob.game.loot.LootTableType;
import com.github.tilcob.game.npc.NpcType;
import com.github.tilcob.game.save.registry.ChestRegistry;
import com.github.tilcob.game.save.states.chest.ChestState;

public class TiledAshleyConfigurator {
    private final Engine engine;
    private final AssetManager assetManager;
    private final World world;
    private final Vector2 tmpVec2;
    private final MapObjects tmpMapObjects;
    private final ChestRegistry chestRegistry;
    private final TiledManager tiledManager;

    public TiledAshleyConfigurator(Engine engine, AssetManager assetManager, World world, ChestRegistry chestRegistry, TiledManager tiledManager) {
        this.engine = engine;
        this.assetManager = assetManager;
        this.world = world;
        this.tmpVec2 = new Vector2();
        this.tmpMapObjects = new MapObjects();
        this.chestRegistry = chestRegistry;
        this.tiledManager = tiledManager;
    }

    public void onLoadObject(TiledMapTileMapObject object) {
        Entity entity = engine.createEntity();
        TiledMapTile tile = object.getTile();
        TextureRegion region = getTextureRegion(tile);
        int z = tile.getProperties().get(Constants.Z, 1, Integer.class);

        entity.add(new Graphic(Color.WHITE.cpy(), region));
        addEntityTransform(
            object.getX(), object.getY(), z,
            region.getRegionWidth(), region.getRegionHeight(),
            object.getScaleX(), object.getScaleY(), entity
        );
        addEntityController(object, entity);
        addEntityMove(tile, entity);
        addEntityAnimation(tile, entity);
        BodyDef.BodyType bodyType = getObjectBodyType(tile);
        addEntityPhysic(tile.getObjects(), bodyType, Vector2.Zero, entity);
        addEntityCameraFollow(object, entity);
        addEntityLife(tile, entity);
        addEntityAttack(tile, entity);
        addEntityChest(object, entity);
        addEntityNpc(object, entity);
        entity.add(new Facing(Facing.FacingDirection.DOWN));
        entity.add(new AnimationFsm(entity));
        entity.add(new Tiled(object));
        entity.add(new MapEntity());

        engine.addEntity(entity);
    }

    private void addEntityNpc(MapObject object, Entity entity) {
        String npcTypeStr = object.getProperties().get(Constants.NPC_TYPE, "", String.class);
        String name = object.getName();
        if (name == null) return;
        if (npcTypeStr.isBlank() || npcTypeStr.equals(NpcType.UNDEFINED.name()) || name.isBlank()) return;

        entity.add(new Npc(NpcType.valueOf(npcTypeStr), name));
        entity.add(new PlayerReference(null));
        entity.add(new Dialog());
        entity.add(new MoveIntent());
        entity.add(new NpcFsm(entity));
    }

    private void addEntityChest(MapObject object, Entity entity) {
        int id = object.getProperties().get(Constants.ID, 0, Integer.class);
        MapAsset map = tiledManager.getCurrentMapAsset();
        if (chestRegistry.contains(map, id)) {
            entity.add(new Chest(chestRegistry.getOrCreate(map, id, null)));
            return;
        }

        boolean hasInventory = object.getProperties().get(Constants.HAS_INVENTORY, false, Boolean.class);
        if (!hasInventory) return;
        String lootStr = object.getProperties().get(Constants.LOOT, "", String.class);
        Array<ItemType> loot;
        ChestState state;
        if (!lootStr.isBlank()) {
            loot = getItemType(lootStr);
            state = chestRegistry.getOrCreate(map, id, loot);
        } else {
            loot = LootTableType.BASIC_CHEST.getLootTable().roll();
            state = chestRegistry.getOrCreate(map, id, loot);
        }

        entity.add(new Chest(state));
    }

    private Array<ItemType> getItemType(String lootStr) {
        Array<ItemType> loot = new Array<>();
        String[] itemTypes = lootStr.split(",");

        for (String itemType : itemTypes) {
            loot.add(ItemType.valueOf(itemType));
        }
        return loot;
    }

    private void addEntityLife(TiledMapTile tile, Entity entity) {
        int life = tile.getProperties().get(Constants.LIFE, 0, Integer.class);
        if (life == 0) return;

        float lifeRegeneration = tile.getProperties().get(Constants.LIFE_REGENERATION, 0f, Float.class);
        entity.add(new Life(life, lifeRegeneration));
    }

    private void addEntityAttack(TiledMapTile tile, Entity entity) {
        float damage = tile.getProperties().get(Constants.DAMAGE, 0f, Float.class);
        if (damage == 0) return;
        float windup = tile.getProperties().get(Constants.ATTACK_WINDUP, Constants.DEFAULT_DAMAGE_DELAY, Float.class);
        float cooldown = tile.getProperties().get(Constants.ATTACK_COOLDOWN, 0f, Float.class);
        String soundAssetStr = tile.getProperties().get(Constants.ATTACK_SOUND, "", String.class);
        SoundAsset soundAsset = null;
        if (!soundAssetStr.isBlank()) {
            soundAsset = SoundAsset.valueOf(soundAssetStr);
        }

        entity.add(new Attack(damage, windup, cooldown, soundAsset));
    }

    private void addEntityCameraFollow(TiledMapTileMapObject object, Entity entity) {
        boolean cameraFollow = object.getProperties().get(Constants.CAMERA_FOLLOW, false, Boolean.class);

        if (!cameraFollow) return;

        entity.add(new CameraFollow());
    }

    private BodyDef.BodyType getObjectBodyType(TiledMapTile tile) {
        String classType = tile.getProperties().get(Constants.TYPE, "", String.class);

        if (Constants.PROP.equals(classType)) {
            return BodyDef.BodyType.StaticBody;
        }

        String bodyTypeStr = tile.getProperties().get(Constants.BODY_TYPE, "DynamicBody", String.class);
        return BodyDef.BodyType.valueOf(bodyTypeStr);
    }

    private void addEntityPhysic(MapObjects mapObjects, BodyDef.BodyType bodyType, Vector2 relativeTo, Entity entity) {
        if (mapObjects.getCount() == 0) return;

        Transform transform = Transform.MAPPER.get(entity);
        Vector2 position = transform.getPosition();
        Vector2 scaling = transform.getScaling();

        Body body = createBody(mapObjects, position, scaling, bodyType, relativeTo, entity);
        entity.add(new Physic(body, transform.getPosition().cpy()));
    }

    private void addEntityPhysic(MapObject mapObject, BodyDef.BodyType bodyType, Vector2 relativeTo, Entity entity) {
        if (tmpMapObjects.getCount() > 0) tmpMapObjects.remove(0);
        tmpMapObjects.add(mapObject);
        addEntityPhysic(tmpMapObjects, bodyType, relativeTo, entity);
    }

    private void addEntityAnimation(TiledMapTile tile, Entity entity) {
        String animationStr = tile.getProperties().get(Constants.ANIMATION, "", String.class);
        if (animationStr.isBlank()) return;

        Animation2D.AnimationType animationType = Animation2D.AnimationType.valueOf(animationStr);
        String atlasAssetStr = tile.getProperties().get(Constants.ATLAS_ASSET, AtlasAsset.OBJECTS.name(), String.class);
        AtlasAsset atlasAsset = AtlasAsset.valueOf(atlasAssetStr);
        TextureAtlas textureAtlas = assetManager.get(atlasAsset);
        FileTextureData textureData = (FileTextureData) tile.getTextureRegion().getTexture().getTextureData();
        String atlasKey = textureData.getFileHandle().nameWithoutExtension();
        float animationSpeed = tile.getProperties().get(Constants.ANIMATION_SPEED, Constants.DEFAULT_ANIMATION_SPEED, Float.class);

        entity.add(new Animation2D(atlasAsset, atlasKey, animationType, Animation.PlayMode.LOOP, animationSpeed));
    }

    private void addEntityMove(TiledMapTile tile, Entity entity) {
        float speed = tile.getProperties().get(Constants.SPEED, 0f, Float.class);
        if (speed == 0) return;

        entity.add(new Move(speed));
    }

    private void addEntityController(TiledMapTileMapObject object, Entity entity) {
        boolean controller = object.getProperties().get(Constants.CONTROLLER, false, Boolean.class);
        if(!controller) return;

        entity.add(new Controller());
    }

    private void addEntityTransform(float x, float y, int z, float w, float h,
                                    float scaleX, float scaleY, Entity entity) {
        Vector2 position = new Vector2(x, y);
        Vector2 size = new Vector2(w, h);
        Vector2 scaling = new Vector2(scaleX, scaleY);

        position.scl(Constants.UNIT_SCALE);
        size.scl(Constants.UNIT_SCALE);

        entity.add(new Transform(position, z, size, scaling, 0));
    }

    private TextureRegion getTextureRegion(TiledMapTile tile) {
        String atlasAssetStr = tile.getProperties().get(Constants.ATLAS_ASSET, AtlasAsset.OBJECTS.name(), String.class);
        AtlasAsset atlasAsset = AtlasAsset.valueOf(atlasAssetStr);
        TextureAtlas textureAtlas = assetManager.get(atlasAsset);
        FileTextureData textureData = (FileTextureData) tile.getTextureRegion().getTexture().getTextureData();
        String atlasKey = textureData.getFileHandle().nameWithoutExtension();
        TextureAtlas.AtlasRegion region = textureAtlas.findRegion(atlasKey + "/" + atlasKey);

        if  (region != null) return region;

        return tile.getTextureRegion();
    }

    public void onLoadTile(TiledMapTile tile, float x, float y) {
        createBody(
            tile.getObjects(),
            new Vector2(x, y),
            Constants.DEFAULT_PHYSIC_SCALING,
            BodyDef.BodyType.StaticBody,
            Vector2.Zero,
            Constants.ENVIRONMENT
        );
    }

    private Body createBody(MapObjects mapObjects,
                            Vector2 position,
                            Vector2 scaling,
                            BodyDef.BodyType bodyType,
                            Vector2 relativeTo,
                            Object userData) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyType;
        bodyDef.position.set(position);
        bodyDef.fixedRotation = true;

        Body body = world.createBody(bodyDef);
        body.setUserData(userData);
        for (MapObject object : mapObjects) {
            FixtureDef fixtureDef = TiledPhysics.fixtureDefOf(object, scaling, relativeTo);
            if (fixtureDef == null) continue;
            Fixture fixture = body.createFixture(fixtureDef);
            fixture.setUserData(object);
            fixtureDef.shape.dispose();

        }
        return body;
    }

    public void onLoadTrigger(Trigger.Type type, MapObject triggerMapObject) {
        if (triggerMapObject instanceof RectangleMapObject rectMapObject) {
            makeRectTriggerEntity(triggerMapObject, rectMapObject, type);
        } else if (triggerMapObject instanceof EllipseMapObject ellipseMapObj) {
            makeEllipseTriggerEntity(triggerMapObject, ellipseMapObj, type);
        } else if (triggerMapObject instanceof PolygonMapObject polygonMapObject) {
            makePolygonTriggerEntity(triggerMapObject, polygonMapObject, type);
        } else if (triggerMapObject instanceof PolylineMapObject polylineMapObject) {
            makePolylineTriggerEntity(triggerMapObject, polylineMapObject, type);
        } else {
            throw new GdxRuntimeException("Unsupported trigger map object: " + triggerMapObject);
        }
    }

    private void makeRectTriggerEntity(MapObject triggerMapObject,
                                       RectangleMapObject rectMapObject, Trigger.Type type) {
        Entity entity = engine.createEntity();
        Rectangle rectangle = rectMapObject.getRectangle();

        addEntityTransform(
            rectangle.getX(), rectangle.getY(), 0,
            rectangle.getWidth(), rectangle.getHeight(),
            1, 1, entity
        );
        addEntityPhysic(
            triggerMapObject, BodyDef.BodyType.StaticBody,
            tmpVec2.set(rectangle.getX(), rectangle.getY()).scl(Constants.UNIT_SCALE), entity
        );
        createTrigger(entity, type, rectMapObject.getProperties(), new Tiled(rectMapObject));
    }

    private void makeEllipseTriggerEntity(MapObject triggerMapObject,
                                          EllipseMapObject ellipseMapObject, Trigger.Type type) {
        Entity entity = engine.createEntity();
        Ellipse ellipse = ellipseMapObject.getEllipse();

        addEntityTransform(
            ellipse.x + ellipse.width * .5f,
            ellipse.y + ellipse.height * .5f, 0,
            ellipse.width, ellipse.height,
            1, 1, entity
        );
        addEntityPhysic(
            triggerMapObject, BodyDef.BodyType.StaticBody,
            tmpVec2.set(ellipse.x + ellipse.width * .5f, ellipse.y + ellipse.height * .5f).scl(Constants.UNIT_SCALE),
            entity
        );
        createTrigger(entity, type, ellipseMapObject.getProperties(), new Tiled(ellipseMapObject));
    }

    private void makePolygonTriggerEntity(MapObject triggerMapObject,
                                          PolygonMapObject polygonMapObject, Trigger.Type type) {
        Entity entity = engine.createEntity();
        Polygon polygon = polygonMapObject.getPolygon();

        float[] vertices = polygon.getVertices();
        float x = vertices[0];
        float y = vertices[1];

        addEntityTransform(
            x, y, 0, 1, 1, 1, 1, entity
        );
        addEntityPhysic(
            triggerMapObject, BodyDef.BodyType.StaticBody,
            tmpVec2.set(x, y).scl(Constants.UNIT_SCALE), entity
        );
        createTrigger(entity, type, polygonMapObject.getProperties(), new Tiled(polygonMapObject));
    }

    private void makePolylineTriggerEntity(MapObject triggerMapObject,
                                           PolylineMapObject polylineMapObject, Trigger.Type type) {
        Entity entity = engine.createEntity();
        Polyline polyline = polylineMapObject.getPolyline();

        float[] vertices = polyline.getVertices();
        float x = vertices[0];
        float y = vertices[1];

        addEntityTransform(
            x, y, 0, 1, 1, 1, 1, entity
        );
        addEntityPhysic(
            triggerMapObject, BodyDef.BodyType.StaticBody,
            tmpVec2.set(x, y).scl(Constants.UNIT_SCALE), entity
        );
        createTrigger(entity, type, polylineMapObject.getProperties(), new Tiled(polylineMapObject));
    }

    private void createTrigger(Entity entity, Trigger.Type type, MapProperties properties, Tiled tile) {
        entity.add(new Trigger(type));
        String questId = properties.get(Constants.QUEST_ID, "", String.class);
        if (!questId.isBlank()) entity.add(new Quest(questId));
        entity.add(tile);
        entity.add(new MapEntity());
        engine.addEntity(entity);
    }
}
