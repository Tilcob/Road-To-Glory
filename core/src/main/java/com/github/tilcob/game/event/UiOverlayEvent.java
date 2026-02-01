package com.github.tilcob.game.event;

public record UiOverlayEvent(Type type) {
    public enum Type {
        OPEN_SETTINGS,
        CLOSE_SETTINGS,
        TOGGLE_SETTINGS,
    }
}
