package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Pool;

public class Id implements Component, Pool.Poolable {
    public static final ComponentMapper<Id> MAPPER = ComponentMapper.getFor(Id.class);

    private int id;

    public Id(int id) {
        this.id = id;
    }
    @Override
    public void reset() {
        id = -1;
    }

    public int getId() {
        return id;
    }
}
