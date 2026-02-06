package com.github.tilcob.game.tiled;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
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
import com.github.tilcob.game.entity.EntityIdGenerator;
import com.github.tilcob.game.indicator.IndicatorVisualDef;
import com.github.tilcob.game.indicator.OverheadIndicatorRegistry;
import com.github.tilcob.game.item.ItemDefinitionRegistry;
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

        entity.add(new EntityId(EntityIdGenerator.next()));
        entity.add(new Graphic(Color.WHITE.cpy(), region));
        addEntityTransform(
            object.getX(), object.getY(), z,
            region.getRegionWidth(), region.getRegionHeight(),
            object.getScaleX(), object.getScaleY(), entity
        );
        addEntityMove(object, tile, entity);
        addEntityAnimation(object, tile, entity);
        BodyDef.BodyType bodyType = getObjectBodyType(tile, object);
        addEntityPhysic(tile.getObjects(), bodyType, Vector2.Zero, entity);
        addEntityLife(object, tile, entity);
        addEntityAttack(object, tile, entity);
        addEntityChest(object, entity);
        addEntityNpc(object, tile, entity);
        addMapIndicatorComponents(object.getProperties(), tile.getProperties(), entity);
        addEntityOccluder(object, tile, entity);
        entity.add(new Facing(Facing.FacingDirection.DOWN));
        entity.add(new AnimationFsm(entity));
        entity.add(new Tiled(object));
        entity.add(new MapEntity());

        engine.addEntity(entity);
    }

    private void addEntityOccluder(MapObject object, TiledMapTile tile, Entity entity) {
        String classType = object.getProperties().containsKey(Constants.TYPE)
            ? object.getProperties().get(Constants.TYPE, "", String.class)
            : tile.getProperties().get(Constants.TYPE, "", String.class);
        if (!classType.equals(Constants.PROP)) return;
        entity.add(new Occluder());
    }

    private void addEntityNpc(MapObject object, TiledMapTile tile, Entity entity) {
        MapProperties properties = object.getProperties();
        String npcTypeStr = properties.containsKey(Constants.NPC_TYPE)
            ? properties.get(Constants.NPC_TYPE, NpcType.UNDEFINED.name(), String.class)
            : tile.getProperties().get(Constants.NPC_TYPE, NpcType.UNDEFINED.name(), String.class);
        float expMultiplier = properties.get(Constants.EXP_MULTIPLIER, 1.0f, Float.class);
        String name = object.getName();
        boolean canWander = properties.get(Constants.CAN_WANDER, false, Boolean.class);
        if (name == null) return;
        if (npcTypeStr.equals(NpcType.UNDEFINED.name()) || name.isBlank()) return;

        NpcType npcType = NpcType.valueOf(npcTypeStr);
        entity.add(new Npc(npcType, name));
        entity.add(new Interactable());
        entity.add(new PlayerReference(null));
        entity.add(new Dialog());
        entity.add(new NpcFsm(entity));
        addNpcRole(properties, tile, entity, npcType);
        if (canWander) {
            entity.add(new MoveIntent());
            entity.add(new WanderTimer());
        }
        entity.add(new AggroMemory());
        entity.add(new Equipment());
        entity.add(new StatModifierComponent());
        if (npcType == NpcType.ENEMY && expMultiplier > 1) entity.add(new ExpMultiplier(expMultiplier));
        Transform transform = Transform.MAPPER.get(entity);
        if (transform != null) entity.add(new SpawnPoint(transform.getPosition()));
        addPatrolRoute(object, entity);
    }

    private void addNpcRole(MapProperties properties, TiledMapTile tile, Entity entity, NpcType npcType) {
        String roleValue = getStringProperty(properties, tile.getProperties(), Constants.NPC_ROLE, Constants.ROLE);
        if (roleValue != null && !roleValue.isBlank()) {
            try {
                entity.add(new NpcRole(NpcRole.Role.valueOf(roleValue)));
                return;
            } catch (IllegalArgumentException ignored) {
                Gdx.app.error("TiledAshleyConfigurator", "Unknown npcRole: " + roleValue);
            }
        }
        if (npcType == NpcType.ENEMY) {
            entity.add(new NpcRole(NpcRole.Role.DANGER));
        }
    }

    private void addMapIndicatorComponents(MapProperties properties, MapProperties tileProperties, Entity entity) {
        boolean hasRoleProperty = hasStringProperty(properties, tileProperties, Constants.NPC_ROLE)
            || hasStringProperty(properties, tileProperties, Constants.ROLE);
        if (hasRoleProperty) {
            addNpcRoleFromProperties(properties, tileProperties, entity);
        }
        boolean indicatorAdded = addOverheadIndicatorFromProperties(properties, tileProperties, entity);
        if (!indicatorAdded && hasRoleProperty && OverheadIndicator.MAPPER.get(entity) == null) {
            addOverheadIndicator(entity, OverheadIndicator.OverheadIndicatorType.QUEST_AVAILABLE, properties, tileProperties);
        }
    }

    private void addNpcRoleFromProperties(MapProperties properties, MapProperties tileProperties, Entity entity) {
        if (NpcRole.MAPPER.get(entity) != null) return;
        String roleValue = getStringProperty(properties, tileProperties, Constants.NPC_ROLE, Constants.ROLE);
        if (roleValue == null || roleValue.isBlank()) return;
        try {
            entity.add(new NpcRole(NpcRole.Role.valueOf(roleValue)));
        } catch (IllegalArgumentException ignored) {
            Gdx.app.error("TiledAshleyConfigurator", "Unknown npcRole: " + roleValue);
        }
    }

    private boolean addOverheadIndicatorFromProperties(MapProperties properties, MapProperties tileProperties, Entity entity) {
        if (OverheadIndicator.MAPPER.get(entity) != null) return true;
        String indicatorValue = getStringProperty(properties, tileProperties, Constants.INDICATOR);
        if (indicatorValue == null || indicatorValue.isBlank()) return false;
        try {
            OverheadIndicator.OverheadIndicatorType indicatorType = OverheadIndicator.OverheadIndicatorType.valueOf(indicatorValue);
            addOverheadIndicator(entity, indicatorType, properties, tileProperties);
            return true;
        } catch (IllegalArgumentException ignored) {
            Gdx.app.error("TiledAshleyConfigurator", "Unknown indicator: " + indicatorValue);
            return false;
        }
    }

    private void addOverheadIndicator(Entity entity,
                                      OverheadIndicator.OverheadIndicatorType indicatorType,
                                      MapProperties properties,
                                      MapProperties tileProperties) {
        Transform transform = Transform.MAPPER.get(entity);
        float offsetY = Constants.DEFAULT_INDICATOR_OFFSET_Y;
        if (transform != null) {
            offsetY = transform.getSize().y + Constants.DEFAULT_INDICATOR_OFFSET_Y - 8f * Constants.UNIT_SCALE;
        }
        IndicatorVisualDef visualDef = OverheadIndicatorRegistry.getVisualDef(indicatorType);
        float baseScale = visualDef == null ? 0.6f : visualDef.defaultScale();

        OverheadIndicator indicator = new OverheadIndicator(
            indicatorType,
            new Vector2(0f, offsetY),
            baseScale,
            Color.WHITE.cpy(),
            true
        );

        Boolean allowBob = getBooleanProperty(properties, tileProperties, Constants.INDICATOR_BOB);
        if (allowBob != null) indicator.setAllowBob(allowBob);
        Boolean allowPulse = getBooleanProperty(properties, tileProperties, Constants.INDICATOR_PULSE);
        if (allowPulse != null) indicator.setAllowPulse(allowPulse);

        entity.add(indicator);
        entity.add(new OverheadIndicatorState());
        entity.add(new OverheadIndicatorAnimation(0f, 0f, 0f, 0f, 1f));
    }

    private void addPatrolRoute(MapObject object, Entity entity) {
        String patrolPointsStr = object.getProperties().get(Constants.PATROL_POINTS, "", String.class);
        if (patrolPointsStr.isBlank()) return;

        Array<Vector2> points = new Array<>();
        String[] pairs = patrolPointsStr.split(";");
        for (String pair : pairs) {
            String[] values = pair.trim().split(",");
            if (values.length != 2) continue;
            float x = Float.parseFloat(values[0].trim());
            float y = Float.parseFloat(values[1].trim());
            points.add(new Vector2(x, y).scl(Constants.UNIT_SCALE));
        }
        if (points.isEmpty()) return;

        boolean loop = object.getProperties().get(Constants.PATROL_LOOP, true, Boolean.class);
        float waitTime = object.getProperties().get(Constants.PATROL_WAIT, Constants.PATROL_WAIT_TIME, Float.class);
        entity.add(new PatrolRoute(points, loop, waitTime));
    }

    private void addEntityChest(MapObject object, Entity entity) {
        int id = object.getProperties().get(Constants.ID, 0, Integer.class);
        MapAsset map = tiledManager.getCurrentMapAsset();
        if (chestRegistry.contains(map, id)) {
            entity.add(new Chest(chestRegistry.getOrCreate(map, id, null)));
            entity.add(new Interactable());
            return;
        }

        boolean hasInventory = object.getProperties().get(Constants.HAS_INVENTORY, false, Boolean.class);
        if (!hasInventory) return;
        String lootStr = object.getProperties().get(Constants.LOOT, "", String.class);
        Array<String> loot;
        ChestState state;
        if (!lootStr.isBlank()) {
            loot = getItemIds(lootStr);
            state = chestRegistry.getOrCreate(map, id, loot);
        } else {
            loot = LootTableType.BASIC_CHEST.getLootTable().roll();
            state = chestRegistry.getOrCreate(map, id, loot);
        }

        entity.add(new Chest(state));
        entity.add(new Interactable());
    }

    private Array<String> getItemIds(String lootStr) {
        Array<String> loot = new Array<>();
        String[] itemTypes = lootStr.split(",");

        for (String itemType : itemTypes) {
            loot.add(ItemDefinitionRegistry.resolveId(itemType.trim()));
        }
        return loot;
    }

    private void addEntityLife(MapObject object, TiledMapTile tile, Entity entity) {
        MapProperties properties = object.getProperties();
        int life = properties.containsKey(Constants.LIFE) ?
            properties.get(Constants.LIFE, 0, Integer.class) :
            tile.getProperties().get(Constants.LIFE, 0, Integer.class);
        if (life == 0) return;

        float lifeRegeneration = properties.containsKey(Constants.LIFE_REGENERATION)
            ? properties.get(Constants.LIFE_REGENERATION, 0f, Float.class)
            : tile.getProperties().get(Constants.LIFE_REGENERATION, 0f, Float.class);
        entity.add(new Life(life, lifeRegeneration));
    }

    private void addEntityAttack(MapObject object, TiledMapTile tile, Entity entity) {
        MapProperties properties = object.getProperties();
        float damage = properties.containsKey(Constants.DAMAGE)
            ? properties.get(Constants.DAMAGE, 0f, Float.class)
            : tile.getProperties().get(Constants.DAMAGE, 0f, Float.class);
        if (damage == 0) return;
        float windup = properties.containsKey(Constants.ATTACK_WINDUP)
            ? properties.get(Constants.ATTACK_WINDUP, Constants.DEFAULT_DAMAGE_DELAY, Float.class)
            : tile.getProperties().get(Constants.ATTACK_WINDUP, Constants.DEFAULT_DAMAGE_DELAY, Float.class);
        float cooldown = properties.containsKey(Constants.ATTACK_COOLDOWN)
            ? properties.get(Constants.ATTACK_COOLDOWN, 0f, Float.class)
            : tile.getProperties().get(Constants.ATTACK_COOLDOWN, 0f, Float.class);
        String soundAssetStr = properties.containsKey(Constants.ATTACK_SOUND)
            ? properties.get(Constants.ATTACK_SOUND, "", String.class)
            : tile.getProperties().get(Constants.ATTACK_SOUND, "", String.class);
        SoundAsset soundAsset = null;
        if (!soundAssetStr.isBlank()) {
            soundAsset = SoundAsset.valueOf(soundAssetStr);
        }

        entity.add(new Attack(damage, windup, cooldown, soundAsset));
    }

    private BodyDef.BodyType getObjectBodyType(TiledMapTile tile, MapObject object) {
        String classType = tile.getProperties().get(Constants.TYPE, "", String.class);
        if (Constants.PROP.equals(classType)) return BodyDef.BodyType.StaticBody;

        MapProperties properties = object.getProperties();
        String bodyTypeStr = properties.containsKey(Constants.BODY_TYPE)
            ? properties.get(Constants.BODY_TYPE, "DynamicBody", String.class)
            : tile.getProperties().get(Constants.BODY_TYPE, "DynamicBody", String.class);
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

    private void addEntityAnimation(MapObject object, TiledMapTile tile, Entity entity) {
        MapProperties properties = object.getProperties();
        String animationStr = properties.containsKey(Constants.ANIMATION)
            ? properties.get(Constants.ANIMATION, "", String.class)
            : tile.getProperties().get(Constants.ANIMATION, "", String.class);
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

    private void addEntityMove(MapObject object, TiledMapTile tile, Entity entity) {
        MapProperties properties = object.getProperties();
        float speed = properties.containsKey(Constants.SPEED)
            ? properties.get(Constants.SPEED, 0f, Float.class)
            : tile.getProperties().get(Constants.SPEED, 0f, Float.class);
        if (speed == 0) return;

        entity.add(new Move(speed));
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
        if (type == Trigger.Type.UNDEFINED) {
            Gdx.app.error("TiledAshleyConfigurator", "Undefined trigger type: " + type);
            return;
        }
        entity.add(new Trigger(type));
        String questId = properties.get(Constants.QUEST_ID, "", String.class);
        if (!questId.isBlank()) entity.add(new Quest(questId));
        String cutsceneId = properties.get(Constants.CUTSCENE_ID, "", String.class);
        if (!cutsceneId.isBlank()) entity.add(new CutsceneReference(cutsceneId));
        addMapIndicatorComponents(properties, null, entity);
        entity.add(tile);
        entity.add(new MapEntity());
        engine.addEntity(entity);
    }

    private String getStringProperty(MapProperties properties, MapProperties tileProperties, String key) {
        if (properties.containsKey(key)) {
            return properties.get(key, "", String.class);
        }
        if (tileProperties != null && tileProperties.containsKey(key)) {
            return tileProperties.get(key, "", String.class);
        }
        return "";
    }

    private String getStringProperty(MapProperties properties,
                                     MapProperties tileProperties,
                                     String key,
                                     String fallbackKey) {
        String value = getStringProperty(properties, tileProperties, key);
        if (value != null && !value.isBlank()) return value;
        return getStringProperty(properties, tileProperties, fallbackKey);
    }

    private boolean hasStringProperty(MapProperties properties, MapProperties tileProperties, String key) {
        String value = getStringProperty(properties, tileProperties, key);
        return value != null && !value.isBlank();
    }

    private Boolean getBooleanProperty(MapProperties properties, MapProperties tileProperties, String key) {
        if (properties.containsKey(key)) {
            return properties.get(key, Boolean.class);
        }
        if (tileProperties != null && tileProperties.containsKey(key)) {
            return tileProperties.get(key, Boolean.class);
        }
        return null;
    }
}
