package com.github.tilcob.game.event;

public record PauseEvent(Action action) {
    public enum Action {
        PAUSE,
        RESUME,
        TOGGLE
    }
}
