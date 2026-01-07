package com.github.tilcob.game.event;

public record AutosaveEvent(AutosaveReason reason) {

    public enum AutosaveReason {
        MAP_CHANGE,
        MANUEL,
        PERIODIC
    }
}


