package com.github.tilcob.game.player;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.component.Life;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.save.states.PlayerState;

public class PlayerStateApplier {
    public static void apply(PlayerState state, Entity player) {
        Transform transform = Transform.MAPPER.get(player);
        Life life = Life.MAPPER.get(player);
        Inventory inventory = Inventory.MAPPER.get(player);

        if (transform == null || life == null || inventory == null) return;

        transform.getPosition().set(state.getPosition());
        life.setLife(state.getLife());
        inventory.getItems().clear();
        inventory.getItemsToAdd().addAll(state.getItems());
    }
}
