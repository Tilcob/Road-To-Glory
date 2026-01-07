package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public record AutosaveEvent(AutosaveReason reason) {

    public enum AutosaveReason {
        MAP_CHANGE,
        MANUEL,
        PERIODIC
    }
}


