package com.github.tilcob.game.player;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.component.Life;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.item.ItemType;
import com.github.tilcob.game.save.states.PlayerState;

import java.util.Map;

public class PlayerStateApplier {
    public static void apply(PlayerState state, Entity player) {
        Transform transform = Transform.MAPPER.get(player);
        Life life = Life.MAPPER.get(player);
        Inventory inventory = Inventory.MAPPER.get(player);

        if (transform == null || life == null || inventory == null) return;

        transform.getPosition().set(state.getPositionAsVector().scl(1 / Constants.UNIT_SCALE));
        life.setLife(state.getLife());
        inventory.getItems().clear();
        Map<ItemType, Integer> items = state.getItems();
        for (var entry : items.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                inventory.getItemsToAdd().add(entry.getKey());
            }
        }
    }
}
