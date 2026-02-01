package com.github.tilcob.game.system.installers;

import com.badlogic.ashley.core.Engine;
import com.github.tilcob.game.system.*;

public class AiSystemsInstaller implements SystemInstaller {
    @Override
    public void install(Engine engine) {
        engine.addSystem(withPriority(new FsmSystem(), SystemOrder.AI));
        engine.addSystem(withPriority(new AiSystem(), SystemOrder.AI));
        engine.addSystem(withPriority(new MoveIntentSystem(), SystemOrder.AI));
        engine.addSystem(withPriority(new NpcPathfindingSystem(), SystemOrder.AI));
    }
}
