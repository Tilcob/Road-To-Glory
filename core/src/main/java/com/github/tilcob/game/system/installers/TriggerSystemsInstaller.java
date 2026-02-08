package com.github.tilcob.game.system.installers;

import com.badlogic.ashley.core.Engine;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.system.TriggerSystem;

public class TriggerSystemsInstaller implements SystemInstaller {
    private final AudioManager audioManager;
    private final GameEventBus eventBus;

    public TriggerSystemsInstaller(AudioManager audioManager, GameEventBus eventBus) {
        this.audioManager = audioManager;
        this.eventBus = eventBus;
    }

    @Override
    public void install(Engine engine) {
        engine.addSystem(withPriority(new TriggerSystem(audioManager, eventBus), SystemOrder.COMBAT_TRIGGER));
    }
}
