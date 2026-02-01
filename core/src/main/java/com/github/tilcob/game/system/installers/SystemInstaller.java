package com.github.tilcob.game.system.installers;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.github.tilcob.game.system.SystemOrder;

public interface SystemInstaller {
    void install(Engine engine);

    default <T extends EntitySystem> T withPriority(T system, SystemOrder order) {
        system.priority = order.priority();
        return system;
    }
}
