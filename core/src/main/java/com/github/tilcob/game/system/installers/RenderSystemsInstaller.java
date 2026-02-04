package com.github.tilcob.game.system.installers;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tilcob.game.assets.AssetManager;
import com.github.tilcob.game.system.*;

public class RenderSystemsInstaller implements SystemInstaller {
    private final Batch batch;
    private final Viewport viewport;
    private final OrthographicCamera camera;
    private final AssetManager assetManager;
    private final World physicWorld;
    private final boolean debug;

    public RenderSystemsInstaller(
            Batch batch,
            Viewport viewport,
            OrthographicCamera camera,
            AssetManager assetManager,
            World physicWorld,
            boolean debug) {
        this.batch = batch;
        this.viewport = viewport;
        this.camera = camera;
        this.assetManager = assetManager;
        this.physicWorld = physicWorld;
        this.debug = debug;
    }

    @Override
    public void install(Engine engine) {
        engine.addSystem(withPriority(new AnimationSystem(assetManager), SystemOrder.RENDER));
        engine.addSystem(withPriority(new OverheadIndicatorAnimationSystem(), SystemOrder.RENDER));
        engine.addSystem(withPriority(new CameraSystem(camera), SystemOrder.RENDER));
        engine.addSystem(withPriority(new RenderSystem(batch, viewport, camera), SystemOrder.RENDER));
        engine.addSystem(withPriority(
            new OverheadIndicatorRenderSystem(assetManager, batch, viewport, camera),
            SystemOrder.RENDER
        ));
        engine.addSystem(withPriority(new ScreenFadeSystem(batch, viewport, camera), SystemOrder.RENDER));
        engine.addSystem(withPriority(new CameraPanSystem(camera), SystemOrder.RENDER));
        if (debug) {
            engine.addSystem(withPriority(
                    new PhysicDebugRenderSystem(physicWorld, camera),
                    SystemOrder.DEBUG_RENDER));
        }
    }
}
