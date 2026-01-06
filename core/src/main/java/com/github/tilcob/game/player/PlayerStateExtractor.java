package com.github.tilcob.game.player;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.save.states.PlayerState;

public class PlayerStateExtractor {
    public static PlayerState fromEntity(Entity entity) {
        Transform transform = Transform.MAPPER.get(entity);
        Life life = Life.MAPPER.get(entity);
        Inventory inventory = Inventory.MAPPER.get(entity);

        if (transform == null || life == null || inventory == null) return new PlayerState();

        return new PlayerState(
            transform.getPosition(),
            life.getLife(),
            inventory.getItems()
        );
    }
}
