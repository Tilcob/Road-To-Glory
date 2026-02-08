package com.github.tilcob.game.system.installers;

import com.badlogic.ashley.core.Engine;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.system.AbilitySystem;
import com.github.tilcob.game.system.ControllerSystem;

public class InputSystemsInstaller implements SystemInstaller {
    private final GameEventBus eventBus;

    public InputSystemsInstaller(GameEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void install(Engine engine) {
        engine.addSystem(withPriority(
                new ControllerSystem(eventBus),
                SystemOrder.INPUT));
        engine.addSystem(withPriority(
            new AbilitySystem(eventBus),
            SystemOrder.INPUT));
    }
}
