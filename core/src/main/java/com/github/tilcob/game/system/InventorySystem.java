package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.*;
import com.github.tilcob.game.item.ItemDefinition;
import com.github.tilcob.game.item.ItemDefinitionRegistry;
import com.github.tilcob.game.quest.QuestManager;

public class InventorySystem extends IteratingSystem implements Disposable {
    private final GameEventBus eventBus;
    private final QuestManager questManager;
    private final Skin skin;
    private Entity player;

    public InventorySystem(GameEventBus eventBus, QuestManager questManager, Skin skin) {
        super(Family.all(Inventory.class).get());
        this.eventBus = eventBus;
        this.questManager = questManager;
        this.skin = skin;

        eventBus.subscribe(DragAndDropEvent.class, this::onMoveEntity);
        eventBus.subscribe(InventoryDropEvent.class, this::onDropItem);
        eventBus.subscribe(SplitStackEvent.class, this::onSplitStack);
    }

    @Override
    protected void processEntity(Entity player, float deltaTime) {
        Inventory inventory = Inventory.MAPPER.get(player);
        this.player = player;
        if (inventory.getItemsToAdd().isEmpty()) return;

        boolean inventoryFull = false;
        boolean addedAny = false;
        var remaining = new Array<String>();
        ObjectIntMap<String> addedCounts = new ObjectIntMap<>();

        for (String itemId : inventory.getItemsToAdd()) {
            String resolvedId = ItemDefinitionRegistry.resolveId(itemId);
            if (inventoryFull) {
                remaining.add(resolvedId);
                continue;
            }

            int slotIndex = emptySlotIndex(inventory);
            if (slotIndex == -1) {
                inventoryFull = true;
                remaining.add(resolvedId);
                continue;
            }
            addItem(inventory, resolvedId, slotIndex);
            addedAny = true;
            addedCounts.getAndIncrement(resolvedId, 0, 1);
        }
        inventory.getItemsToAdd().clear();
        inventory.getItemsToAdd().addAll(remaining);
        if (inventoryFull) eventBus.fire(new InventoryFullEvent());
        if (addedAny) {
            eventBus.fire(new EntityAddItemEvent(player));
            for (var entry : addedCounts) {
                questManager.signal(player, "collect", entry.key, entry.value);
            }
        }
    }

    private void addItem(Inventory inventory, String itemId, int slotIndex) {
        Entity stackEntity = findStackableItem(inventory, itemId);
        if (stackEntity != null) {
            Item stack = Item.MAPPER.get(stackEntity);
            stack.add(1);
            return;
        }
        inventory.add(spawnItem(itemId, slotIndex, inventory.nextId()));
    }

    private Entity spawnItem(String itemId, int slotIndex, int id) {
        Entity entity = getEngine().createEntity();
        entity.add(new Item(itemId, slotIndex, 1));
        entity.add(new Id(id));
        getEngine().addEntity(entity);

        eventBus.fire(new ItemCreatedEvent(id, entity));
        return entity;
    }

    private void removeItem(Entity entity) {
        int id = Id.MAPPER.get(entity).getId();
        eventBus.fire(new ItemRemovedEvent(id));
        getEngine().removeEntity(entity);
    }

    private int emptySlotIndex(Inventory inventory) {
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

    private void onDropItem(InventoryDropEvent event) {
        Inventory inventory = Inventory.MAPPER.get(player);
        if (inventory == null) return;

        Entity itemEntity = findItemAtSlot(inventory, event.slotIndex());
        if (itemEntity == null) return;

        Item item = Item.MAPPER.get(itemEntity);
        String itemId = item.getItemId();
        int count = item.getCount();

        inventory.remove(itemEntity);
        removeItem(itemEntity);

        Entity droppedEntity = getEngine().createEntity();
        droppedEntity.add(new Item(itemId, -1, count));
        droppedEntity.add(new MapEntity());

        TextureRegion region = skin.getRegion(ItemDefinitionRegistry.get(itemId).icon());
        droppedEntity.add(new Graphic(Color.WHITE.cpy(), region));
        droppedEntity.add(createDropTransform(region));

        getEngine().addEntity(droppedEntity);
        eventBus.fire(new UpdateInventoryEvent(player));
    }

    private Transform createDropTransform(TextureRegion region) {
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

    private Vector2 getFacingOffset() {
        Facing facing = Facing.MAPPER.get(player);
        if (facing == null) return new Vector2(0f, -1f);

        return switch (facing.getDirection()) {
            case LEFT -> new Vector2(-1f, 0f);
            case RIGHT -> new Vector2(1f, 0f);
            case UP -> new Vector2(0f, 1f);
            case DOWN -> new Vector2(0f, -1f);
        };
    }

    private void onMoveEntity(DragAndDropEvent event) {
        Inventory inventory = Inventory.MAPPER.get(player);

        Entity fromEntity = findItemAtSlot(inventory, event.fromIdx());
        if (fromEntity == null) return;

        Entity toEntity = findItemAtSlot(inventory, event.toIdx());

        Item from = Item.MAPPER.get(fromEntity);

        if (tryStack(fromEntity, from, toEntity, inventory)) {
            eventBus.fire(new UpdateInventoryEvent(player));
            return;
        }

        swapOrMove(from, toEntity, event.fromIdx(), event.toIdx());
        eventBus.fire(new UpdateInventoryEvent(player));
    }

    private void swapOrMove(Item from, Entity toEntity, int fromSlot, int toSlot) {
        if (toEntity != null) {
            Item to = Item.MAPPER.get(toEntity);
            to.setSlotIndex(fromSlot);
        }

        from.setSlotIndex(toSlot);
    }

    private boolean tryStack(Entity fromEntity, Item from, Entity toEntity, Inventory inventory) {
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
            getEngine().removeEntity(fromEntity);
        }

        return true;
    }

    private Entity findItemAtSlot(Inventory inventory, int slot) {
        for (Entity e : inventory.getItems()) {
            if (Item.MAPPER.get(e).getSlotIndex() == slot) {
                return e;
            }
        }
        return null;
    }

    private Entity findStackableItem(Inventory inventory, String itemId) {
        ItemDefinition definition = ItemDefinitionRegistry.get(itemId);
        for (Entity e : inventory.getItems()) {
            Item item = Item.MAPPER.get(e);
            if (item.getItemId().equals(itemId) && definition.isStackable() && item.getCount() < definition.maxStack()) {
                return e;
            }
        }
        return null;
    }

    private void onSplitStack(SplitStackEvent event) {
        Inventory inventory = Inventory.MAPPER.get(player);
        Entity itemEntity = findItemAtSlot(inventory, event.slotIndex());
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

    @Override
    public void dispose() {
        eventBus.unsubscribe(DragAndDropEvent.class, this::onMoveEntity);
        eventBus.unsubscribe(InventoryDropEvent.class, this::onDropItem);
        eventBus.unsubscribe(SplitStackEvent.class, this::onSplitStack);
    }
}
