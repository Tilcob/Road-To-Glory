package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.item.ItemType;

public class Inventory implements Component {
    public static final ComponentMapper<Inventory> MAPPER = ComponentMapper.getFor(Inventory.class);

    private final Array<Entity> entities = new Array<>();
    private final Array<ItemType> itemsToAdd = new Array<>();
    private int nextItemId = 0;

    public Array<Entity> getItems() {
        return entities;
    }

    public Array<ItemType> getItemsToAdd() {
        return itemsToAdd;
    }

    public void add(Entity entity) {
        entities.add(entity);
    }

    public void remove(Entity fromEntity) {
        entities.removeValue(fromEntity, true);
    }

    public int nextId() {
        return ++nextItemId;
    }

    public void syncId(int id) {
        nextItemId = Math.max(nextItemId, id);
    }
}
