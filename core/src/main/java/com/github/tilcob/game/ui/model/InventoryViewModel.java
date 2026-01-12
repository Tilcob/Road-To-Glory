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

public class InventoryViewModel extends ViewModel {
    private final Array<ItemModel> items = new Array<>();
    private boolean open = false;

    public InventoryViewModel(GameServices services) {
        super(services);

        getEventBus().subscribe(UpdateInventoryEvent.class, this::updateInventory);
        getEventBus().subscribe(EntityAddItemEvent.class, this::onEntityAddItemEvent);
        getEventBus().subscribe(UiEvent.class, this::onUiEvent);
        getEventBus().subscribe(InventoryFullEvent.class, this::onFullInventory);
        getEventBus().subscribe(UpdateQuestLogEvent.class, this::onQuestLogEvent);
    }

    private void onQuestLogEvent(UpdateQuestLogEvent event) {
        QuestLog questLog = QuestLog.MAPPER.get(event.player());
        this.propertyChangeSupport.firePropertyChange(Constants.ADD_QUESTS, null, questLog.getQuests());
    }

    private void updateInventory(UpdateInventoryEvent updateInventoryEvent) {
        Inventory inventory = Inventory.MAPPER.get(updateInventoryEvent.player());
        if (inventory == null) return;
        items.clear();
        onAddItem(inventory.getItems());
        propertyChangeSupport.firePropertyChange(Constants.ADD_ITEMS_TO_INVENTORY, null, items);
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

            ItemModel model = new ItemModel(
                idComp.getId(),
                item.getItemType().getCategory(),
                item.getItemType().getDrawableName(),
                item.getSlotIndex(),
                item.isEquipped(),
                item.getCount()
            );
            this.items.add(model);
        }
    }

    private void onFullInventory(InventoryFullEvent inventoryFullEvent) {

    }

    @Override
    public void dispose() {
        getEventBus().unsubscribe(UpdateInventoryEvent.class, this::updateInventory);
        getEventBus().unsubscribe(EntityAddItemEvent.class, this::onEntityAddItemEvent);
        getEventBus().unsubscribe(UiEvent.class, this::onUiEvent);
        getEventBus().unsubscribe(InventoryFullEvent.class, this::onFullInventory);
        getEventBus().unsubscribe(UpdateQuestLogEvent.class, this::onQuestLogEvent);
    }
}
