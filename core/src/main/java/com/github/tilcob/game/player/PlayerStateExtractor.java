package com.github.tilcob.game.player;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.component.Item;
import com.github.tilcob.game.component.Life;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.save.states.PlayerState;

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
        for (Entity itemEntity : inventory.getItems()) {
            Item item = Item.MAPPER.get(itemEntity);
            if (item == null) continue;
            for (int i = 0; i < item.getCount(); i++) {
                state.getItemsByName().add(item.getItemId());
            }
        }
        return state;
    }
}
