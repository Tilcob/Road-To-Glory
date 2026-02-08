package com.github.tilcob.game.system.installers;

import com.badlogic.ashley.core.Engine;
import com.github.tilcob.game.ai.state.NpcStateSupport;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.system.AiSystem;
import com.github.tilcob.game.system.FsmSystem;
import com.github.tilcob.game.system.MoveIntentSystem;
import com.github.tilcob.game.system.NpcPathfindingSystem;

public class AiSystemsInstaller implements SystemInstaller {
    private final GameEventBus eventBus;

    public AiSystemsInstaller(GameEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void install(Engine engine) {
        NpcStateSupport.setEventBus(eventBus);
        engine.addSystem(withPriority(new FsmSystem(), SystemOrder.AI));
        engine.addSystem(withPriority(new AiSystem(), SystemOrder.AI));
        engine.addSystem(withPriority(new MoveIntentSystem(), SystemOrder.AI));
        engine.addSystem(withPriority(new NpcPathfindingSystem(), SystemOrder.AI));
    }
}
