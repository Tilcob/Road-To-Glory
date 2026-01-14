package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectSet;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.npc.NpcType;

public class AttackSystem extends IteratingSystem {
    private static final Rectangle attackAABB = new Rectangle();
    private final AudioManager audioManager;
    private final World world;
    private final Vector2 tmpVertex;
    private final ObjectSet<Entity> hitEntities;
    private Body attackerBody;
    private Entity attackerEntity;
    private float attackDamage;

    public AttackSystem(World world, AudioManager audioManager) {
        super(Family.all(Attack.class, Facing.class, Physic.class).get());
        this.world = world;
        this.audioManager = audioManager;
        this.tmpVertex = new Vector2();
        this.hitEntities = new ObjectSet<>();
        this.attackerBody = null;
        this.attackerEntity = null;
        this.attackDamage = 0;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Attack attack = Attack.MAPPER.get(entity);

        if (attack.consumeStarted()) {
            if (attack.getSfx() != null) audioManager.playSound(attack.getSfx());
            setRooted(entity, true);
            hitEntities.clear();
        }

        if (attack.canAttack()) return;

        attack.advance(deltaTime);
        if (attack.consumeTriggered()) {
            setRooted(entity, false);
            Facing.FacingDirection facingDirection = Facing.MAPPER.get(entity).getDirection();
            attackerEntity = entity;
            attackerBody = Physic.MAPPER.get(entity).getBody();
            PolygonShape attackShape = getAttackFixture(attackerBody, facingDirection);
            updateAttackAABB(attackerBody.getPosition(), attackShape);

            attackDamage = attack.getDamage();
            world.QueryAABB(this::attackCallback, attackAABB.x, attackAABB.y, attackAABB.x + attackAABB.width, attackAABB.y + attackAABB.height);
        }

        if (attack.consumeFinished()) {
            setRooted(entity, false);
        }
    }

    private static void setRooted(Entity entity, boolean rooted) {
        Move move = Move.MAPPER.get(entity);
        if (move != null) {
            move.setRooted(rooted);
        }
    }

    private void updateAttackAABB(Vector2 position, PolygonShape attackShape) {
        int vertexCount = attackShape.getVertexCount();
        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;

        for (int i = 0; i < vertexCount; i++) {
            attackShape.getVertex(i, tmpVertex);
            tmpVertex.add(position);
            minX = Math.min(minX, tmpVertex.x);
            minY = Math.min(minY, tmpVertex.y);
            maxX = Math.max(maxX, tmpVertex.x);
            maxY = Math.max(maxY, tmpVertex.y);
        }
        attackAABB.set(minX, minY, maxX - minX, maxY - minY);
    }

    private boolean attackCallback(Fixture fixture) {
        Body body = fixture.getBody();

        if (body.equals(attackerBody)) return true;
        if (!(body.getUserData() instanceof Entity entity)) return true;

        Npc npc = Npc.MAPPER.get(entity);
        if (npc != null && npc.getType() == NpcType.FRIEND) return true;

        Life life = Life.MAPPER.get(entity);
        if (life == null) {
            return true;
        }

        if (hitEntities.contains(entity)) return true;
        hitEntities.add(entity);

        Damaged damaged = Damaged.MAPPER.get(entity);
        if (damaged == null) {
            entity.add(new Damaged(attackDamage, attackerEntity));
        } else {
            damaged.addDamage(attackDamage, attackerEntity);
        }
        return true;
    }

    private PolygonShape getAttackFixture(Body body, Facing.FacingDirection facingDirection) {
        Array<Fixture> fixtureList = body.getFixtureList();
        String fixtureName = Constants.ATTACK_SENSOR + facingDirection.getAtlasKey();
        for (Fixture fixture : fixtureList) {
            if ((getName(fixture) != null && fixtureName.equals(getName(fixture)))
                && Shape.Type.Polygon.equals(fixture.getShape().getType())) {
                return (PolygonShape) fixture.getShape();
            }
        }

        throw new GdxRuntimeException("Entity has no attack sensors of name: " + fixtureName);
    }

    private String getName(Fixture fixture) {
        if (fixture.getUserData() instanceof MapObject mapObject) {
            return mapObject.getName();
        } else {
            return fixture.getUserData() != null ? fixture.getUserData().toString() : null;
        }
    }
}
