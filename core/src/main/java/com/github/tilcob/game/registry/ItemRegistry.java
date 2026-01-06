package com.github.tilcob.game.registry;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.IntMap;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.ItemCreatedEvent;
import com.github.tilcob.game.event.ItemRemovedEvent;

public class ItemRegistry {
    private final IntMap<Entity> itemsById = new IntMap<>();

    public ItemRegistry(GameEventBus eventBus) {
        eventBus.subscribe(ItemCreatedEvent.class, this::register);
        eventBus.subscribe(ItemRemovedEvent.class, this::unregister);
    }

    private void unregister(ItemRemovedEvent itemRemovedEvent) {
        itemsById.remove(itemRemovedEvent.id());
    }

    private void register(ItemCreatedEvent itemCreatedEvent) {
        itemsById.put(itemCreatedEvent.id(), itemCreatedEvent.entity());
    }

    public Entity get(int id) {
        return itemsById.get(id);
    }
}
