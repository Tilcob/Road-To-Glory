package com.github.tilcob.game.trigger;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.github.tilcob.game.audio.AudioManager;

public class ChestTriggerHandler implements TriggerHandler {
    private final AudioManager audioManager;

    public ChestTriggerHandler(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    @Override
    public void execute(Entity trigger, Entity triggeringEntity) {
        Gdx.app.log("ChestTriggerHandler", "Executing chest trigger");
    }
}
