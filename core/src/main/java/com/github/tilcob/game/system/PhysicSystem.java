package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.ExitTriggerEvent;
import com.github.tilcob.game.event.GameEventBus;

public class PhysicSystem extends IteratingSystem implements EntityListener, ContactListener {
    private final World world;
    private final float interval;
    private final GameEventBus eventBus;
    private float accumulator;

    public PhysicSystem(World world, float interval, GameEventBus eventBus) {
        super(Family.all(Physic.class, Transform.class).get());
        this.world = world;
        this.interval = interval;
        this.eventBus = eventBus;
        this.accumulator = 0;
        world.setContactListener(this);
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        engine.addEntityListener(getFamily(), this);
    }

    @Override
    public void removedFromEngine(Engine engine) {
        super.removedFromEngine(engine);
        engine.removeEntityListener(this);
    }

    @Override
    public void entityAdded(Entity entity) {}

    @Override
    public void entityRemoved(Entity entity) {
        Physic physic = Physic.MAPPER.get(entity);
        if (physic != null && physic.getBody() != null) {
            world.destroyBody(physic.getBody());
        }
    }

    @Override
    public void update(float deltaTime) {
        accumulator += deltaTime;

        while (accumulator >= interval) {
            accumulator -= interval;
            super.update(interval);
            world.step(interval, 6, 2);
        }
        world.clearForces();

        // interpolation

        float alpha = accumulator / interval; // alpha is between [0,1) for render interpolation

        for (int i = 0; i < getEntities().size(); i++) {
            interpolateEntity(getEntities().get(i), alpha);
        }

    }

    private void interpolateEntity(Entity entity, float alpha) {
        Transform transform = Transform.MAPPER.get(entity);
        Physic physic = Physic.MAPPER.get(entity);
        // interpolate between previous and current physics step positions for smooth rendering
        transform.getPosition().set(
            MathUtils.lerp(physic.getPrevPosition().x, physic.getBody().getPosition().x, alpha),
            MathUtils.lerp(physic.getPrevPosition().y, physic.getBody().getPosition().y, alpha)
        );

    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Physic physic = Physic.MAPPER.get(entity);
        physic.getPrevPosition().set(physic.getBody().getPosition());
    }

    // Don't add or remove bodies here!!!!

    @Override
    public void beginContact(Contact contact) {
        Collision collision = getCollision(contact);
        if (collision == null) return;

        if (isPlayer(collision.entityA, collision.fixtureA) && isAttackFixture(collision.fixtureA)) return;
        if (isPlayer(collision.entityB, collision.fixtureB) && isAttackFixture(collision.fixtureB)) return;
        onEnter(collision.entityA, collision.fixtureA, collision.entityB, collision.fixtureB);
    }

    private void onEnter(Entity entityA, Fixture fixtureA, Entity entityB, Fixture fixtureB) {
        if (hasTriggerComponent(entityA) && isPlayer(entityB, fixtureB)) {
            Trigger trigger = Trigger.MAPPER.get(entityA);
            trigger.add(entityB);
        } else if (hasTriggerComponent(entityB) && isPlayer(entityA, fixtureA)) {
            Trigger trigger = Trigger.MAPPER.get(entityB);
            trigger.add(entityA);
        } else if (isSensor(fixtureA) && isPlayer(entityB, fixtureB)) {
            addTrigger(fixtureA, entityA, entityB);
        } else if (isSensor(fixtureB) && isPlayer(entityA, fixtureA)) {
            addTrigger(fixtureB, entityB, entityA);
        }
    }

    @Override
    public void endContact(Contact contact) {
        Collision collision = getCollision(contact);
        if (collision == null) return;

        if (isPlayer(collision.entityA, collision.fixtureA) && isAttackFixture(collision.fixtureA)) return;
        if (isPlayer(collision.entityB, collision.fixtureB) && isAttackFixture(collision.fixtureB)) return;

        onExit(collision.entityA, collision.fixtureA, collision.entityB, collision.fixtureB);
    }

    private void onExit(Entity entityA, Fixture fixtureA, Entity entityB, Fixture fixtureB) {
        if (hasTriggerComponent(entityA) && isPlayer(entityB, fixtureB)) {
            Trigger trigger = Trigger.MAPPER.get(entityA);
            trigger.remove(entityB);
            eventBus.fire(new ExitTriggerEvent(entityB, entityA));
        } else if (hasTriggerComponent(entityB) && isPlayer(entityA, fixtureA)) {
            Trigger trigger = Trigger.MAPPER.get(entityB);
            trigger.remove(entityA);
            eventBus.fire(new ExitTriggerEvent(entityA, entityB));
        } else if (isSensor(fixtureA) && isPlayer(entityB, fixtureB)) {
            if (hasTriggerComponent(entityA)) {
                Trigger trigger = Trigger.MAPPER.get(entityA);
                trigger.remove(entityB);
            }
            eventBus.fire(new ExitTriggerEvent(entityB, entityA));
        } else if (isSensor(fixtureB) && isPlayer(entityA, fixtureA)) {
            if (hasTriggerComponent(entityB)) {
                Trigger trigger = Trigger.MAPPER.get(entityB);
                trigger.remove(entityA);
            }
            eventBus.fire(new ExitTriggerEvent(entityA, entityB));
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

    private static void addTrigger(Fixture trigger, Entity triggerEntity, Entity player) {
        MapObject mapObject = (MapObject) trigger.getUserData();
        if (mapObject == null) return;

        String typeStr = mapObject.getProperties().get(Constants.TRIGGER_TYPE, "", String.class);
        String questId = mapObject.getProperties().get(Constants.QUEST_ID, "", String.class);
        if (typeStr.isBlank()) throw new GdxRuntimeException("Missing or false trigger type: " + typeStr);

        Trigger triggerComp = new Trigger(Trigger.Type.valueOf(typeStr));
        triggerComp.add(player);
        triggerEntity.add(triggerComp);
        if (!questId.isBlank()) triggerEntity.add(new Quest(questId));
    }

    private boolean isSensor(Fixture fixture) {
        if (fixture.getUserData() instanceof MapObject mapObject) {
            return mapObject.getProperties().get(Constants.TYPE, "", String.class)
                .equals(Constants.TRIGGER_CLASS)
                && mapObject.getProperties().get(Constants.SENSOR, false, Boolean.class);
        }
        return false;
    }

    private boolean hasTriggerComponent(Entity triggerEntity) {
        Trigger trigger = Trigger.MAPPER.get(triggerEntity);
        return trigger != null;
    }

    private boolean isPlayer(Entity entity, Fixture fixture) {
        return Player.MAPPER.get(entity) != null && !fixture.isSensor();
    }

    private boolean isAttackFixture(Fixture fixture) {
        if (fixture.getUserData() instanceof MapObject mapObject) {
            return mapObject.getName().contains("attack")
                && mapObject.getProperties().get(Constants.SENSOR, false, Boolean.class);
        }
        return false;
    }

    private Collision getCollision(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Object userDataA = fixtureA.getBody().getUserData();
        Fixture fixtureB = contact.getFixtureB();
        Object userDataB = fixtureB.getBody().getUserData();

        if (!(userDataA instanceof Entity entityA) || !(userDataB instanceof Entity entityB)) return null;
        return new Collision(fixtureA, fixtureB, entityA, entityB);
    }

    public record Collision(Fixture fixtureA, Fixture fixtureB, Entity entityA, Entity entityB) {
    }
}
