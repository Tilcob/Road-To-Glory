package com.github.tilcob.game.inventory;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.ItemCreatedEvent;
import com.github.tilcob.game.event.ItemRemovedEvent;
import com.github.tilcob.game.event.UpdateInventoryEvent;
import com.github.tilcob.game.item.ItemCategory;
import com.github.tilcob.game.item.ItemDefinition;
import com.github.tilcob.game.item.ItemDefinitionRegistry;
import com.github.tilcob.game.quest.QuestManager;
import com.github.tilcob.game.stat.StatType;

public class InventoryService {
    private final GameEventBus eventBus;
    private Engine engine;
    private final QuestManager questManager;
    private Skin skin;
    private Entity player;

    public InventoryService(GameEventBus eventBus, QuestManager questManager) {
        this.eventBus = eventBus;
        this.questManager = questManager;
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

    public void moveEntity(int toIndex, int fromIndex) {
        Inventory inventory = Inventory.MAPPER.get(player);

        Entity fromEntity = findItemAtSlot(inventory, fromIndex);
        if (fromEntity == null) return;

        Entity toEntity = findItemAtSlot(inventory, toIndex);

        Item from = Item.MAPPER.get(fromEntity);

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
        if (definition == null || definition.category() != category) return;
        if (!meetsRequirements(player, definition)) return;

        equipment.equip(category, itemEntity);
        eventBus.fire(new UpdateInventoryEvent(player));
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
            return;
        }

        swapOrMove(from, toEntity, from.getSlotIndex(), toIndex);
        eventBus.fire(new UpdateInventoryEvent(player));
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
}
