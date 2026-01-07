package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.tilcob.game.component.Chest;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.component.OpenChestRequest;

public class ChestSystem extends IteratingSystem {

    public ChestSystem() {
        super(Family.all(OpenChestRequest.class).get());
    }

    @Override
    protected void processEntity(Entity player, float deltaTime) {
        Entity chestEntity = OpenChestRequest.MAPPER.get(player).getChest();
        Chest chest = Chest.MAPPER.get(chestEntity);
        if (chest == null) return;

        if (chest.isOpen()) {
            Inventory inventory = Inventory.MAPPER.get(player);
            if (inventory == null) {
                close(player, chest);
                return;
            }

            inventory.getItemsToAdd().addAll(chest.getContents());
            chest.clear();
            close(player, chest);
        }
    }

    private void close(Entity player, Chest chest) {
        player.remove(OpenChestRequest.class);
        chest.close();
    }
}
