package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.World;
import com.github.tilcob.game.component.Physic;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.event.GameEventBus;

public class PhysicSystem extends IteratingSystem implements EntityListener {
    private final World world;
    private final float interval;
    private float accumulator;

    public PhysicSystem(World world, float interval, GameEventBus eventBus) {
        super(Family.all(Physic.class, Transform.class).get());
        this.world = world;
        this.interval = interval;
        this.accumulator = 0;
        world.setContactListener(new GameContactListener(eventBus));
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
    public void entityAdded(Entity entity) {
    }

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

        float alpha = accumulator / interval;

        for (int i = 0; i < getEntities().size(); i++) {
            interpolateEntity(getEntities().get(i), alpha);
        }

    }

    private void interpolateEntity(Entity entity, float alpha) {
        Transform transform = Transform.MAPPER.get(entity);
        Physic physic = Physic.MAPPER.get(entity);
        // interpolate between previous and current physics step positions for smooth
        // rendering
        transform.getPosition().set(
                MathUtils.lerp(physic.getPrevPosition().x, physic.getBody().getPosition().x, alpha),
                MathUtils.lerp(physic.getPrevPosition().y, physic.getBody().getPosition().y, alpha));

    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Physic physic = Physic.MAPPER.get(entity);
        physic.getPrevPosition().set(physic.getBody().getPosition());
    }
}
