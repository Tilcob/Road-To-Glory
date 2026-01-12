package com.github.tilcob.game.player;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.github.tilcob.game.assets.AssetManager;
import com.github.tilcob.game.assets.AtlasAsset;
import com.github.tilcob.game.assets.SoundAsset;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.tiled.TiledPhysics;

public class PlayerFactory {

    public static Entity create(Engine engine, AssetManager assetManager, World world) {
        Entity entity = engine.createEntity();
        TiledMapTile player = assetManager.getPlayerTile();
        TextureRegion region = getTextureRegion(player, assetManager);

        entity.add(new Graphic(Color.WHITE.cpy(), region));

        entity.add(new Transform(
            new Vector2().scl(Constants.UNIT_SCALE),
            1,
            new Vector2(region.getRegionWidth(),region.getRegionHeight()).scl(Constants.UNIT_SCALE),
            new Vector2(1,1),
            0
        ));

        entity.add(new Player());
        entity.add(new Controller());
        float speed = player.getProperties().get(Constants.SPEED, 0f, Float.class);
        entity.add(new Move(speed));
        addEntityAnimation(player, entity);
        BodyDef.BodyType bodyType = BodyDef.BodyType.DynamicBody;
        addEntityPhysic(assetManager.getPlayerTile().getObjects(), bodyType, Vector2.Zero, entity, world);
        entity.add(new CameraFollow());
        addEntityLife(player, entity);
        addEntityAttack(player, entity);
        entity.add(new Facing(Facing.FacingDirection.DOWN));
        entity.add(new AnimationFsm(entity));
        entity.add(new Inventory());
        entity.add(new QuestLog());

        engine.addEntity(entity);
        return entity;
    }

    private static void addEntityAttack(TiledMapTile tile, Entity entity) {
        float damage = tile.getProperties().get(Constants.DAMAGE, 0f, Float.class);
        if (damage == 0) return;
        float damageDelay = tile.getProperties().get(Constants.DAMAGE_DELAY, 0f, Float.class);
        float windup = tile.getProperties().get(Constants.ATTACK_WINDUP, damageDelay, Float.class);
        float cooldown = tile.getProperties().get(Constants.ATTACK_COOLDOWN, 0f, Float.class);
        String soundAssetStr = tile.getProperties().get(Constants.ATTACK_SOUND, "", String.class);
        SoundAsset soundAsset = null;
        if (!soundAssetStr.isBlank()) {
            soundAsset = SoundAsset.valueOf(soundAssetStr);
        }

        entity.add(new Attack(damage, windup, cooldown, soundAsset));
    }

    private static void addEntityLife(TiledMapTile player, Entity entity) {
        float life = player.getProperties().get(Constants.LIFE, 0, Integer.class);
        float lifeRegeneration = player.getProperties().get(Constants.LIFE_REGENERATION, 0f, Float.class);

        entity.add(new Life(life, lifeRegeneration));
    }

    private static void addEntityPhysic(MapObjects mapObjects, BodyDef.BodyType bodyType, Vector2 relativeTo, Entity entity, World world) {
        if (mapObjects.getCount() == 0) return;

        Transform transform = Transform.MAPPER.get(entity);
        Vector2 position = transform.getPosition();
        Vector2 scaling = transform.getScaling();

        Body body = createBody(mapObjects, position, scaling, bodyType, relativeTo, entity, world);
        entity.add(new Physic(body, transform.getPosition().cpy()));

    }

    private static Body createBody(MapObjects mapObjects, Vector2 position, Vector2 scaling, BodyDef.BodyType bodyType, Vector2 relativeTo, Entity entity, World world) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyType;
        bodyDef.position.set(position);
        bodyDef.fixedRotation = true;

        Body body = world.createBody(bodyDef);
        body.setUserData(entity );
        for (MapObject object : mapObjects) {
            FixtureDef fixtureDef = TiledPhysics.fixtureDefOf(object, scaling, relativeTo);
            if (fixtureDef == null) continue;
            Fixture fixture = body.createFixture(fixtureDef);
            fixture.setUserData(object.getName());
            fixtureDef.shape.dispose();

        }
        return body;
    }

    private static void addEntityAnimation(TiledMapTile tile, Entity entity) {
        AtlasAsset atlasAsset = AtlasAsset.OBJECTS;
        FileTextureData textureData = (FileTextureData) tile.getTextureRegion().getTexture().getTextureData();
        String atlasKey = textureData.getFileHandle().nameWithoutExtension();

        String typeStr = tile.getProperties().get(Constants.ANIMATION, "", String.class);
        Animation2D.AnimationType type = Animation2D.AnimationType.valueOf(typeStr);
        float speed = tile.getProperties().get(Constants.ANIMATION_SPEED, Constants.DEFAULT_ANIMATION_SPEED, Float.class);

        entity.add(new Animation2D(atlasAsset, atlasKey, type, Animation.PlayMode.LOOP, speed));
    }

    private static TextureRegion getTextureRegion(TiledMapTile tile, AssetManager assetManager) {
        TextureAtlas textureAtlas = assetManager.get(AtlasAsset.OBJECTS);
        FileTextureData textureData = (FileTextureData) tile.getTextureRegion().getTexture().getTextureData();
        String atlasKey = textureData.getFileHandle().nameWithoutExtension();
        TextureAtlas.AtlasRegion region = textureAtlas.findRegion(atlasKey + "/" + atlasKey);

        if  (region != null) return region;

        return tile.getTextureRegion();
    }
}
