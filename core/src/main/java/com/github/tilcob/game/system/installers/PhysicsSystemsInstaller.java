package com.github.tilcob.game.system.installers;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.physics.box2d.World;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.system.FacingSystem;
import com.github.tilcob.game.system.PhysicMoveSystem;
import com.github.tilcob.game.system.PhysicSystem;
import com.github.tilcob.game.system.SystemOrder;

public class PhysicsSystemsInstaller implements SystemInstaller {
    private final World physicWorld;
    private final GameEventBus eventBus;

    public PhysicsSystemsInstaller(World physicWorld, GameEventBus eventBus) {
        this.physicWorld = physicWorld;
        this.eventBus = eventBus;
    }

    @Override
    public void install(Engine engine) {
        engine.addSystem(withPriority(new PhysicMoveSystem(), SystemOrder.PHYSICS));
        engine.addSystem(withPriority(new FacingSystem(), SystemOrder.PHYSICS));
        engine.addSystem(withPriority(
                new PhysicSystem(physicWorld, Constants.FIXED_INTERVAL, eventBus),
                SystemOrder.PHYSICS));
    }
}
