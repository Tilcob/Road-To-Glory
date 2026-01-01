package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.utils.Array;

public class EntityAddItemEvent extends Event {
    private final Entity entity;

    public EntityAddItemEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
