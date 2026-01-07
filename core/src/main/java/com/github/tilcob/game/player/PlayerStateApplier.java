package com.github.tilcob.game.player;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.component.Life;
import com.github.tilcob.game.component.Physic;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.item.ItemType;
import com.github.tilcob.game.save.states.PlayerState;

public class PlayerStateApplier {
    public static void apply(PlayerState state, Entity player) {
        Transform transform = Transform.MAPPER.get(player);
        Life life = Life.MAPPER.get(player);
        Inventory inventory = Inventory.MAPPER.get(player);
        Physic physic = Physic.MAPPER.get(player);

        if (transform == null || life == null || inventory == null) return;

        Vector2 position = state.getPositionAsVector().cpy();
        transform.getPosition().set(position);
        physic.getBody().setTransform(transform.getPosition(), 0);
        life.setLife(state.getLife());
        inventory.getItems().clear();
        for (var item : state.getItemsByName()) {
            inventory.getItemsToAdd().add(ItemType.valueOf(item));
        }
    }
}
