package com.github.tilcob.game.system;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;

public class PhysicDebugRenderSystem extends EntitySystem implements Disposable {
    private final World world;
    private final Box2DDebugRenderer debugRenderer;
    private final Camera camera;

    public PhysicDebugRenderSystem(World world, Camera camera) {
        this.world = world;
        this.camera = camera;
        debugRenderer = new Box2DDebugRenderer();
        // setProcessing(true); to dynamically turn on/off the system
    }

    @Override
    public void update(float deltaTime) {
        debugRenderer.render(world, camera.combined);
    }

    @Override
    public void dispose() {
        debugRenderer.dispose();
    }
}
