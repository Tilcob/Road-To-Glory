package com.github.tilcob.game.player;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.inventory.InventoryService;
import com.github.tilcob.game.item.ItemCategory;
import com.github.tilcob.game.item.ItemDefinitionRegistry;
import com.github.tilcob.game.save.states.PlayerState;

public class PlayerStateApplier {
    public static void apply(PlayerState state, Entity player, InventoryService inventoryService) {
        Transform transform = Transform.MAPPER.get(player);
        Life life = Life.MAPPER.get(player);
        Inventory inventory = Inventory.MAPPER.get(player);
        Physic physic = Physic.MAPPER.get(player);
        Equipment equipment = Equipment.MAPPER.get(player);

        if (transform == null || life == null || inventory == null) return;

        Vector2 position = state.getPositionAsVector().cpy();
        transform.getPosition().set(position);
        physic.getBody().setTransform(transform.getPosition(), 0);
        life.setLife(state.getLife());
        inventory.getItems().clear();
        if (inventoryService != null) {
            inventoryService.setPlayer(player);
        }

        if (state.getItemSlots() != null && !state.getItemSlots().isEmpty() && inventoryService != null) {
            for (PlayerState.ItemSlotState slotState : state.getItemSlots()) {
                if (slotState == null) continue;
                String resolved = ItemDefinitionRegistry.resolveId(slotState.getItemId());
                if (!ItemDefinitionRegistry.isKnownId(resolved)) continue;
                Entity itemEntity = inventoryService.spawnItem(resolved, slotState.getSlotIndex(), inventory.nextId());
                Item item = Item.MAPPER.get(itemEntity);
                if (item != null && slotState.getCount() > 1) {
                    item.add(slotState.getCount() - 1);
                }
                inventory.add(itemEntity);
            }
        } else {
            for (var item : state.getItemsByName()) {
                inventory.getItemsToAdd().add(ItemDefinitionRegistry.resolveId(item));
            }
        }

        if (equipment != null && state.getEquipmentSlots() != null) {
            for (ItemCategory category : ItemCategory.values()) {
                if (equipment.getEquipped(category) != null) {
                    equipment.unequip(category);
                }
            }
            for (var entry : state.getEquipmentSlots().entrySet()) {
                PlayerState.EquipmentSlotState slotState = entry.getValue();
                if (slotState == null) continue;
                Entity itemEntity = findItemForEquipment(inventory, slotState);
                if (itemEntity == null) continue;
                Item item = Item.MAPPER.get(itemEntity);
                if (item != null) {
                    item.setSlotIndex(-1);
                }
                equipment.equip(entry.getKey(), itemEntity);
            }
        }
    }

    private static Entity findItemForEquipment(Inventory inventory, PlayerState.EquipmentSlotState slotState) {
        if (slotState.getSlotIndex() >= 0) {
            for (Entity entity : inventory.getItems()) {
                Item item = Item.MAPPER.get(entity);
                if (item != null && item.getSlotIndex() == slotState.getSlotIndex()) {
                    return entity;
                }
            }
        }
        for (Entity entity : inventory.getItems()) {
            Item item = Item.MAPPER.get(entity);
            if (item != null && item.getItemId().equals(slotState.getItemId())) {
                return entity;
            }
        }
        return null;
    }
}
