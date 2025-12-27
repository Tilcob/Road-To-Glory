package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.config.Constants;

public class AttackSystem extends IteratingSystem {
    private static final Rectangle attackAABB = new Rectangle();
    private final AudioManager audioManager;
    private final World world;
    private final Vector2 tmpVertex;
    private Body attackerBody;
    private float attackDamage;

    public AttackSystem(World world, AudioManager audioManager) {
        super(Family.all(Attack.class, Facing.class, Physic.class).get());
        this.world = world;
        this.audioManager = audioManager;
        this.tmpVertex = new Vector2();
        this.attackerBody = null;
        this.attackDamage = 0;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Attack attack = Attack.MAPPER.get(entity);

        if (attack.canAttack()) return;

        if (attack.hasAttackStarted()) {
            if (attack.getSfx() != null) audioManager.playSound(attack.getSfx());
            Move move = Move.MAPPER.get(entity);
            if (move != null) {
                move.setRooted(true);
            }
        }

        attack.decreaseAttackTimer(deltaTime);
        if (attack.canAttack()) {
            Facing.FacingDirection facingDirection = Facing.MAPPER.get(entity).getDirection();
            attackerBody = Physic.MAPPER.get(entity).getBody();
            PolygonShape attackShape = getAttackFixture(attackerBody, facingDirection);
            updateAttackAABB(attackerBody.getPosition(), attackShape);

            attackDamage = attack.getDamage();
            world.QueryAABB(this::attackCallback, attackAABB.x, attackAABB.y, attackAABB.width, attackAABB.height);

            Move move = Move.MAPPER.get(entity);
            if (move != null) {
                move.setRooted(false);
            }

        }
    }

    private void updateAttackAABB(Vector2 position, PolygonShape attackShape) {
        attackShape.getVertex(0, tmpVertex);
        tmpVertex.add(position);
        attackAABB.setPosition(tmpVertex.x, tmpVertex.y);

        attackShape.getVertex(2, tmpVertex);
        tmpVertex.add(position);
        attackAABB.setSize(tmpVertex.x, tmpVertex.y);
    }

    private boolean attackCallback(Fixture fixture) {
        Body body = fixture.getBody();

        if (body.equals(attackerBody)) return true;
        if (!(body.getUserData() instanceof Entity entity)) return true;

        Life life = Life.MAPPER.get(entity);
        if (life == null) {
            return true;
        }

        Damaged damaged = Damaged.MAPPER.get(entity);
        if (damaged == null) {
            entity.add(new Damaged(attackDamage));
        } else {
            damaged.addDamage(attackDamage);
        }

        return true;
    }

    private PolygonShape getAttackFixture(Body body, Facing.FacingDirection facingDirection) {
        Array<Fixture> fixtureList = body.getFixtureList();
        String fixtureName = Constants.ATTACK_SENSOR + facingDirection.getAtlasKey();
        for (Fixture fixture : fixtureList) {
            if (fixtureName.equals(fixture.getUserData()) && Shape.Type.Polygon.equals(fixture.getShape().getType())) {
                return (PolygonShape) fixture.getShape();
            }
        }

        throw new GdxRuntimeException("Entity has no attack sensors of name: " + fixtureName);
    }
}
