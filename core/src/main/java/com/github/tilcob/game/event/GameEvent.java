package com.github.tilcob.game.event;

public interface GameEvent {
    boolean isHandled();
    void setHandled(boolean handled);

    default void handle() {
        if (!isHandled()) return;
        setHandled(true);
    }
}
