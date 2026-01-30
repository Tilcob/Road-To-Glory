package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Pool;

public class EntityId implements Component, Pool.Poolable {
    public static final ComponentMapper<EntityId> MAPPER = ComponentMapper.getFor(EntityId.class);

    private long id;

    public EntityId() {
        this.id = -1L;
    }

    public EntityId(long id) {
        this.id = id;
    }

    @Override
    public void reset() {
        this.id = -1L;
    }

    public long getId() {
        return id;
    }
}
