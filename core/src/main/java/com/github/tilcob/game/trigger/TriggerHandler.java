package com.github.tilcob.game.trigger;

import com.badlogic.ashley.core.Entity;

public interface TriggerHandler {
    void onEnter(Entity trigger, Entity triggeringEntity);
    void onExit(Entity trigger, Entity triggeringEntity);
}
