package com.github.tilcob.game.input;

import com.badlogic.ashley.core.Entity;

public class ActiveEntityReference {
    private Entity activeReference;

    public Entity get() {
        return activeReference;
    }

    public void set(Entity activeReference) {
        this.activeReference = activeReference;
    }
}
