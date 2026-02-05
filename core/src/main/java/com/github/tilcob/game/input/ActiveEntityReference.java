package com.github.tilcob.game.input;

import com.badlogic.ashley.core.Entity;

public class ActiveEntityReference {
    private Entity activeReference;
    private Entity focusedEntity;

    public Entity get() {
        return activeReference;
    }

    public void set(Entity activeReference) {
        this.activeReference = activeReference;
    }

    public Entity getFocused() {
        return focusedEntity;
    }

    public void setFocused(Entity focusedEntity) {
        this.focusedEntity = focusedEntity;
    }

    public void clearFocused() {
        this.focusedEntity = null;
    }
}
