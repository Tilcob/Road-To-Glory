package com.github.tilcob.game.ui.model;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.component.Id;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.component.Item;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.*;
import com.github.tilcob.game.input.Command;
import com.github.tilcob.game.item.ItemDefinition;
import com.github.tilcob.game.item.ItemDefinitionRegistry;

public class InventoryViewModel extends ViewModel {
    private final Array<ItemModel> items = new Array<>();
    private boolean open = false;
    private boolean paused = false;

    public InventoryViewModel(GameServices services) {
        super(services);

        getEventBus().subscribe(UpdateInventoryEvent.class, this::updateInventory);
        getEventBus().subscribe(UpdateEquipmentEvent.class, this::updateEquipment);
        getEventBus().subscribe(EntityAddItemEvent.class, this::onEntityAddItemEvent);
        getEventBus().subscribe(UiEvent.class, this::onUiEvent);
        getEventBus().subscribe(InventoryFullEvent.class, this::onFullInventory);
        getEventBus().subscribe(UpdateQuestLogEvent.class, this::onQuestLogEvent);
        getEventBus().subscribe(PauseEvent.class, this::onPauseEvent);
    }

    private void onQuestLogEvent(UpdateQuestLogEvent event) {
        QuestLog questLog = QuestLog.MAPPER.get(event.player());
        this.propertyChangeSupport.firePropertyChange(Constants.ADD_QUESTS, null, questLog.getQuests());
    }

    private void updateInventory(UpdateInventoryEvent updateInventoryEvent) {
        rebuildInventory(updateInventoryEvent.player());
    }

    private void updateEquipment(UpdateEquipmentEvent updateEquipmentEvent) {
        rebuildInventory(updateEquipmentEvent.player());
    }

    private void onEntityAddItemEvent(EntityAddItemEvent event) {
        Inventory inventory = Inventory.MAPPER.get(event.entity());
        if (inventory == null) return;
        onAddItem(inventory.getItems());
        this.propertyChangeSupport.firePropertyChange(Constants.ADD_ITEMS_TO_INVENTORY, null, items);
    }

    private void onUiEvent(UiEvent event) {
        if (event.action() == UiEvent.Action.RELEASE) return;
        if (event.command() == Command.INVENTORY) {
            if (paused) return;
            boolean old = open;
            open = !open;
            this.propertyChangeSupport.firePropertyChange(Constants.OPEN_INVENTORY, old, open);
        }
    }

    private void onAddItem(Array<Entity> items) {
        for (Entity itemEntity : items) {
            Item item = Item.MAPPER.get(itemEntity);
            Id idComp = Id.MAPPER.get(itemEntity);
            if (idComp == null) return;

            ItemDefinition definition = ItemDefinitionRegistry.get(item.getItemId());
            ItemModel model = new ItemModel(
                idComp.getId(),
                definition.category(),
                definition.name(),
                definition.icon(),
                item.getSlotIndex(),
                item.isEquipped(),
                item.getCount()
            );
            this.items.add(model);
        }
    }

    private void rebuildInventory(Entity player) {
        Inventory inventory = Inventory.MAPPER.get(player);
        if (inventory == null) return;
        items.clear();
        onAddItem(inventory.getItems());
        propertyChangeSupport.firePropertyChange(Constants.ADD_ITEMS_TO_INVENTORY, null, items);
    }

    private void onFullInventory(InventoryFullEvent inventoryFullEvent) {

    }

    private void onPauseEvent(PauseEvent event) {
        if (event == null) return;
        switch (event.action()) {
            case PAUSE -> {
                paused = true;
                closeInventory();
            }
            case RESUME -> paused = false;
            case TOGGLE -> {
                paused = !paused;
                if (paused) closeInventory();
            }
        }
    }

    private void closeInventory() {
        if (!open) return;
        boolean old = true;
        open = false;
        this.propertyChangeSupport.firePropertyChange(Constants.OPEN_INVENTORY, old, false);
    }

    @Override
    public void dispose() {
        getEventBus().unsubscribe(UpdateInventoryEvent.class, this::updateInventory);
        getEventBus().unsubscribe(UpdateEquipmentEvent.class, this::updateEquipment);
        getEventBus().unsubscribe(EntityAddItemEvent.class, this::onEntityAddItemEvent);
        getEventBus().unsubscribe(UiEvent.class, this::onUiEvent);
        getEventBus().unsubscribe(InventoryFullEvent.class, this::onFullInventory);
        getEventBus().unsubscribe(UpdateQuestLogEvent.class, this::onQuestLogEvent);
        getEventBus().unsubscribe(PauseEvent.class, this::onPauseEvent);
    }
}
