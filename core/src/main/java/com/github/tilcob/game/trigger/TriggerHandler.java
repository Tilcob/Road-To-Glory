package com.github.tilcob.game.trigger;

import com.badlogic.ashley.core.Entity;

public interface TriggerHandler {
    void execute(Entity trigger, Entity triggeringEntity);
}
