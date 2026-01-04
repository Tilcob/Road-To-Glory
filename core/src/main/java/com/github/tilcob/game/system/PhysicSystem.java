package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.*;
import com.github.tilcob.game.component.Physic;
import com.github.tilcob.game.component.Player;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.component.Trigger;

public class PhysicSystem extends IteratingSystem implements EntityListener, ContactListener {
    private final World world;
    private final float interval;
    private float accumulator;

    public PhysicSystem(World world, float interval) {
        super(Family.all(Physic.class, Transform.class).get());
        this.world = world;
        this.interval = interval;
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
        if (physic != null) {
            world.destroyBody(physic.getBody());
        }
    }

    @Override
    public void update(float deltaTime) {
        accumulator += deltaTime;

        while (accumulator >= interval) {
            accumulator -= interval;
            super.update(deltaTime);
            world.step(interval, 6, 2);
        }
        world.clearForces();

        // interpolation

        float alpha = accumulator / interval; // alpha is between [0,1)

        for (int i = 0; i < getEntities().size(); i++) {
            interpolateEntity(getEntities().get(i), alpha);
        }

    }

    private void interpolateEntity(Entity entity, float alpha) {
        Transform transform = Transform.MAPPER.get(entity);
        Physic physic = Physic.MAPPER.get(entity);

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
        Fixture fixtureA = contact.getFixtureA();
        Object userDataA = fixtureA.getBody().getUserData();
        Fixture fixtureB = contact.getFixtureB();
        Object userDataB = fixtureB.getBody().getUserData();

        if (!(userDataA instanceof Entity entityA) || !(userDataB instanceof Entity entityB)) return;

        onEnter(entityA, fixtureA, entityB, fixtureB);
    }

    private void onEnter(Entity entityA, Fixture fixtureA, Entity entityB, Fixture fixtureB) {
        Trigger trigger = Trigger.MAPPER.get(entityA);
        boolean isPlayer = Player.MAPPER.get(entityB) != null && !fixtureB.isSensor();
        if (trigger != null && isPlayer) {
            trigger.add(entityB);
            return;
        }

        trigger = Trigger.MAPPER.get(entityB);
        isPlayer = Player.MAPPER.get(entityA) != null && !fixtureA.isSensor();
        if (trigger != null && isPlayer) {
            trigger.add(entityA);
        }
    }

    @Override
    public void endContact(Contact contact) {
    }

    private void onExit(Entity entityA, Fixture fixtureA, Entity entityB, Fixture fixtureB) {
        Trigger trigger = Trigger.MAPPER.get(entityA);
        boolean isPlayer = Player.MAPPER.get(entityB) != null && !fixtureB.isSensor();
        if (trigger != null && isPlayer) {
            trigger.remove(entityB);
            return;
        }

        trigger = Trigger.MAPPER.get(entityB);
        isPlayer = Player.MAPPER.get(entityA) != null && !fixtureA.isSensor();
        if (trigger != null && isPlayer) {
            trigger.remove(entityA);
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }
}
