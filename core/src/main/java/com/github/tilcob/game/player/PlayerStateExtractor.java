package com.github.tilcob.game.player;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.item.ItemType;
import com.github.tilcob.game.save.states.PlayerState;
import java.util.HashMap;
import java.util.Map;

public class PlayerStateExtractor {
    public static PlayerState fromEntity(Entity entity) {
        Transform transform = Transform.MAPPER.get(entity);
        Life life = Life.MAPPER.get(entity);
        Inventory inventory = Inventory.MAPPER.get(entity);
        PlayerState state = new PlayerState();

        if (transform == null || life == null || inventory == null) return state;

        state.setPosX(transform.getPosition().x);
        state.setPosY(transform.getPosition().y);
        state.setLife(life.getLife());
        Map<ItemType, Integer> items = state.getItems();
        for (Entity itemEntity : inventory.getItems()) {
            Item item = Item.MAPPER.get(itemEntity);
            if (item == null) continue;
            items.merge(item.getItemType(), item.getCount(), Integer::sum);
        }
        state.setItems(items);
        return state;
    }
}
