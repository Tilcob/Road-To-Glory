package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;

public class InteractEvent {
    private final Entity player;
    private boolean handled;

    public InteractEvent(Entity player) {
        this.player = player;
    }

    public Entity getPlayer() {
        return player;
    }

    public boolean isHandled() {
        return handled;
    }

    public void setHandled(boolean handled) {
        this.handled = handled;
    }
}
