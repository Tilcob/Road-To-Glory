package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;

public class Inventory implements Component {
    public static final ComponentMapper<Inventory> MAPPER = ComponentMapper.getFor(Inventory.class);

    private final Array<Entity> entities = new Array<>();

    public Array<Entity> getEntities() {
        return entities;
    }

    public void add(Entity entity) {
        entities.add(entity);
    }
}
