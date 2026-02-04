package com.github.tilcob.game.inventory;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.*;
import com.github.tilcob.game.item.ItemCategory;
import com.github.tilcob.game.item.ItemDefinition;
import com.github.tilcob.game.item.ItemDefinitionRegistry;
import com.github.tilcob.game.quest.QuestManager;
import com.github.tilcob.game.stat.StatType;

public class InventoryService {
    private final GameEventBus eventBus;
    private Engine engine;
    private Skin skin;
    private Entity player;

    public InventoryService(GameEventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void setPlayer(Entity player) {
        this.player = player;
    }

    public void setSkin(Skin skin) {
        this.skin = skin;
    }

    public void addItem(Inventory inventory, String itemId, int slotIndex) {
        Entity stackEntity = findStackableItem(inventory, itemId);
        if (stackEntity != null) {
            Item stack = Item.MAPPER.get(stackEntity);
            stack.add(1);
            return;
        }
        inventory.add(spawnItem(itemId, slotIndex, inventory.nextId()));
    }

    public Entity spawnItem(String itemId, int slotIndex, int id) {
        Entity entity = engine.createEntity();
        entity.add(new Item(itemId, slotIndex, 1));
        entity.add(new Id(id));
        engine.addEntity(entity);

        eventBus.fire(new ItemCreatedEvent(id, entity));
        return entity;
    }

    public void removeItem(Entity entity) {
        int id = Id.MAPPER.get(entity).getId();
        eventBus.fire(new ItemRemovedEvent(id));
        engine.removeEntity(entity);
    }

    public int emptySlotIndex(Inventory inventory) {
        outer:
        for (int i = 0; i < Constants.INVENTORY_CAPACITY; i++) {
            for (var item : inventory.getItems()) {
                if (Item.MAPPER.get(item).getSlotIndex() == i) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    public boolean moveChestContents(Chest chest, int fromIdx, int toIdx) {
        if (chest == null) return false;
        if (fromIdx == toIdx) return false;

        Array<String> contents = chest.getContents();
        if (fromIdx < 0 || fromIdx >= contents.size) return false;
        if (toIdx < 0 || toIdx >= contents.size) return false;

        String fromItem = contents.get(fromIdx);
        String toItem = contents.get(toIdx);
        contents.set(fromIdx, toItem);
        contents.set(toIdx, fromItem);
        chest.setContents(contents);
        return true;
    }

    public void moveEntity(int toIndex, int fromIndex) {
        Inventory inventory = Inventory.MAPPER.get(player);

        Entity fromEntity = findItemAtSlot(inventory, fromIndex);
        if (fromEntity == null) return;

        Item from = Item.MAPPER.get(fromEntity);
        Entity toEntity = findItemAtSlot(inventory, toIndex);

        if (tryStack(fromEntity, from, toEntity, inventory)) {
            eventBus.fire(new UpdateInventoryEvent(player));
            return;
        }

        swapOrMove(from, toEntity, fromIndex, toIndex);
        eventBus.fire(new UpdateInventoryEvent(player));
    }

    public void equipItem(ItemCategory category, int fromIndex) {
        Inventory inventory = Inventory.MAPPER.get(player);
        Equipment equipment = Equipment.MAPPER.get(player);
        if (inventory == null || equipment == null) return;

        Entity itemEntity = findItemAtSlot(inventory, fromIndex);
        if (itemEntity == null) return;

        Item item = Item.MAPPER.get(itemEntity);
        ItemDefinition definition = ItemDefinitionRegistry.get(item.getItemId());
        if (definition.category() != category) return;
        if (!meetsRequirements(player, definition)) return;

        equipment.equip(category, itemEntity);
        eventBus.fire(new UpdateInventoryEvent(player));
        eventBus.fire(new UpdateEquipmentEvent(player));
    }

    public void unequipItem(ItemCategory category, int toIndex) {
        Inventory inventory = Inventory.MAPPER.get(player);
        Equipment equipment = Equipment.MAPPER.get(player);
        if (inventory == null || equipment == null) return;

        Entity equippedEntity = equipment.getEquipped(category);
        if (equippedEntity == null) return;

        equipment.unequip(category);

        Item from = Item.MAPPER.get(equippedEntity);
        if (from == null) return;

        Entity toEntity = findItemAtSlot(inventory, toIndex);
        if (toEntity == equippedEntity) return;

        if (tryStack(equippedEntity, from, toEntity, inventory)) {
            eventBus.fire(new UpdateInventoryEvent(player));
            eventBus.fire(new UpdateEquipmentEvent(player));
            return;
        }

        swapOrMove(from, toEntity, from.getSlotIndex(), toIndex);
        eventBus.fire(new UpdateInventoryEvent(player));
        eventBus.fire(new UpdateEquipmentEvent(player));
    }

    public void splitStack(int slotIndex) {
        Inventory inventory = Inventory.MAPPER.get(player);
        Entity itemEntity = findItemAtSlot(inventory, slotIndex);
        if (itemEntity == null) return;

        Item item = Item.MAPPER.get(itemEntity);
        if (item.getCount() < 2) return;

        int half = item.getCount() / 2;
        item.remove(half);

        int emptySlot = emptySlotIndex(inventory);
        if (emptySlot == -1) return;

        Entity newItemEntity = spawnItem(item.getItemId(), emptySlot, inventory.nextId());
        Item newItem = Item.MAPPER.get(newItemEntity);
        newItem.add(half - 1);
        newItem.setSlotIndex(emptySlot);

        inventory.add(newItemEntity);

        eventBus.fire(new UpdateInventoryEvent(player));
    }

    public void dropItem(int slotIndex) {
        Inventory inventory = Inventory.MAPPER.get(player);
        if (inventory == null) return;

        Entity itemEntity = findItemAtSlot(inventory, slotIndex);
        if (itemEntity == null) return;

        Item item = Item.MAPPER.get(itemEntity);
        String itemId = item.getItemId();
        int count = item.getCount();

        inventory.remove(itemEntity);
        removeItem(itemEntity);

        Entity droppedEntity = engine.createEntity();
        droppedEntity.add(new Item(itemId, -1, count));
        droppedEntity.add(new MapEntity());

        TextureRegion region = skin.getRegion(ItemDefinitionRegistry.get(itemId).icon());
        droppedEntity.add(new Graphic(Color.WHITE.cpy(), region));
        droppedEntity.add(createDropTransform(region));

        engine.addEntity(droppedEntity);
        eventBus.fire(new UpdateInventoryEvent(player));
    }

    public boolean meetsRequirements(Entity entity, ItemDefinition definition) {
        if (definition.requirements().isEmpty()) {
            return true;
        }
        StatComponent stats = StatComponent.MAPPER.get(entity);
        if (stats == null) {
            return false;
        }
        for (var entry : definition.requirements().entrySet()) {
            StatType statType = entry.getKey();
            float required = entry.getValue();
            if (stats.getFinalStat(statType) < required) {
                return false;
            }
        }
        return true;
    }

    public void swapOrMove(Item from, Entity toEntity, int fromSlot, int toSlot) {
        if (toEntity != null) {
            Item to = Item.MAPPER.get(toEntity);
            to.setSlotIndex(fromSlot);
        }

        from.setSlotIndex(toSlot);
    }

    public boolean tryStack(Entity fromEntity, Item from, Entity toEntity, Inventory inventory) {
        if (toEntity == null) return false;

        Item to = Item.MAPPER.get(toEntity);

        if (!from.getItemId().equals(to.getItemId())) return false;
        ItemDefinition definition = ItemDefinitionRegistry.get(from.getItemId());
        if (!definition.isStackable()) return false;

        int space = definition.maxStack() - to.getCount();
        if (space <= 0) return false;

        int transfer = Math.min(space, from.getCount());
        to.add(transfer);
        from.remove(transfer);

        if (from.getCount() == 0) {
            inventory.remove(fromEntity);
            engine.removeEntity(fromEntity);
        }

        return true;
    }

    public Entity findItemAtSlot(Inventory inventory, int slot) {
        for (Entity e : inventory.getItems()) {
            if (Item.MAPPER.get(e).getSlotIndex() == slot) {
                return e;
            }
        }
        return null;
    }

    public Entity findStackableItem(Inventory inventory, String itemId) {
        ItemDefinition definition = ItemDefinitionRegistry.get(itemId);
        for (Entity e : inventory.getItems()) {
            Item item = Item.MAPPER.get(e);
            if (item.getItemId().equals(itemId) && definition.isStackable() && item.getCount() < definition.maxStack()) {
                return e;
            }
        }
        return null;
    }

    public Transform createDropTransform(TextureRegion region) {
        Transform playerTransform = Transform.MAPPER.get(player);
        Vector2 dropPosition = playerTransform == null
            ? new Vector2()
            : playerTransform.getPosition().cpy().add(getFacingOffset());

        Vector2 size = region == null
            ? new Vector2(0f, 0f)
            : new Vector2(region.getRegionWidth(), region.getRegionHeight()).scl(Constants.UNIT_SCALE);

        return new Transform(
            dropPosition,
            1,
            size,
            new Vector2(1f, 1f),
            0f
        );
    }

    public Vector2 getFacingOffset() {
        Facing facing = Facing.MAPPER.get(player);
        if (facing == null) return new Vector2(0f, -1f);

        return switch (facing.getDirection()) {
            case LEFT -> new Vector2(-1f, 0f);
            case RIGHT -> new Vector2(1f, 0f);
            case UP -> new Vector2(0f, 1f);
            case DOWN -> new Vector2(0f, -1f);
        };
    }

    public void transferPlayerToChest(int fromIndex, Entity openChestEntity, Entity openPlayer) {
        if (openChestEntity == null || openPlayer == null) return;
        Chest chest = Chest.MAPPER.get(openChestEntity);
        Inventory inventory = Inventory.MAPPER.get(openPlayer);
        if (chest == null || inventory == null) return;

        setPlayer(openPlayer);
        Entity itemEntity = findItemAtSlot(inventory, fromIndex);
        if (itemEntity == null) return;

        Item item = Item.MAPPER.get(itemEntity);
        if (item == null) return;

        Array<String> contents = chest.getContents();
        int available = Constants.INVENTORY_CAPACITY - contents.size;
        if (available <= 0) return;

        int moveCount = Math.min(available, item.getCount());
        for (int i = 0; i < moveCount; i++) {
            contents.add(item.getItemId());
        }

        if (moveCount >= item.getCount()) {
            inventory.remove(itemEntity);
            removeItem(itemEntity);
        } else {
            item.remove(moveCount);
        }

        chest.setContents(contents);
        eventBus.fire(new UpdateInventoryEvent(openPlayer));
        eventBus.fire(new UpdateChestInventoryEvent(openPlayer, openChestEntity));
    }

    public void transferChestToPlayer(int fromIndex, int toIndex, Entity openChestEntity, Entity openPlayer, QuestManager questManager) {
        if (openChestEntity == null || openPlayer == null) return;
        Chest chest = Chest.MAPPER.get(openChestEntity);
        Inventory inventory = Inventory.MAPPER.get(openPlayer);
        if (chest == null || inventory == null) return;

        Array<String> contents = chest.getContents();
        if (fromIndex < 0 || fromIndex >= contents.size) return;

        String itemId = ItemDefinitionRegistry.resolveId(contents.get(fromIndex));
        if (toIndex < 0 || toIndex >= Constants.INVENTORY_CAPACITY) return;
        setPlayer(openPlayer);
        Entity targetEntity = findItemAtSlot(inventory, toIndex);
        if (targetEntity != null) {
            Item targetItem = Item.MAPPER.get(targetEntity);
            if (targetItem == null) return;
            if (targetItem.getItemId().equals(itemId)) {
                ItemDefinition definition = ItemDefinitionRegistry.get(itemId);
                if (definition.isStackable() && targetItem.getCount() < definition.maxStack()) {
                    targetItem.add(1);
                    contents.removeIndex(fromIndex);
                    chest.setContents(contents);

                    questManager.signal(openPlayer, "collect", itemId, 1);
                    eventBus.fire(new UpdateInventoryEvent(openPlayer));
                    eventBus.fire(new UpdateChestInventoryEvent(openPlayer, openChestEntity));
                }
            }
            return;
        }
        Entity newItem = spawnItem(itemId, toIndex, inventory.nextId());
        inventory.add(newItem);
        contents.removeIndex(fromIndex);
        chest.setContents(contents);

        questManager.signal(openPlayer, "collect", itemId, 1);
        eventBus.fire(new UpdateInventoryEvent(openPlayer));
        eventBus.fire(new UpdateChestInventoryEvent(openPlayer, openChestEntity));
    }
}
