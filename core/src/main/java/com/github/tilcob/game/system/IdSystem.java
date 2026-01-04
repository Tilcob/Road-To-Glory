package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.IntMap;
import com.github.tilcob.game.component.Id;
import com.github.tilcob.game.component.Item;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.ItemCreatedEvent;
import com.github.tilcob.game.event.ItemRemovedEvent;

public class IdSystem extends EntitySystem {
    private final IntMap<Entity> itemsById = new IntMap<>();

    public IdSystem(GameEventBus eventBus) {
        eventBus.subscribe(ItemCreatedEvent.class, this::onItemCreated);
        eventBus.subscribe(ItemRemovedEvent.class, this::onItemRemoved);
    }

    @Override
    public void addedToEngine(Engine engine) {
        ImmutableArray<Entity> entities = engine.getEntitiesFor(Family.all(Item.class, Id.class).get());
        for (Entity entity : entities) {
            Id id = Id.MAPPER.get(entity);
            itemsById.put(id.getId(), entity);
        }
    }

    private void onItemRemoved(ItemRemovedEvent itemRemovedEvent) {
        itemsById.remove(itemRemovedEvent.id());
    }

    private void onItemCreated(ItemCreatedEvent itemCreatedEvent) {
        itemsById.put(itemCreatedEvent.id(), itemCreatedEvent.entity());
    }

    public Entity getEntityById(int id) {
        return itemsById.get(id);
    }
}
