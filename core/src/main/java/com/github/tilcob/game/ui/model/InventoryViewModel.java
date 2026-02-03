package com.github.tilcob.game.ui.model;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.component.QuestLog;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.*;
import com.github.tilcob.game.input.Command;
import com.github.tilcob.game.item.ItemModel;
import com.github.tilcob.game.ui.UiModelFactory;

public class InventoryViewModel extends ViewModel {
    private final Array<ItemModel> items = new Array<>();
    private boolean open = false;
    private boolean paused = false;
    private boolean isChestOpen = false;

    private final UiModelFactory uiModelFactory;

    public InventoryViewModel(GameServices services, UiModelFactory uiModelFactory) {
        super(services);
        this.uiModelFactory = uiModelFactory;

        getEventBus().subscribe(UpdateInventoryEvent.class, this::updateInventory);
        getEventBus().subscribe(UpdateEquipmentEvent.class, this::updateEquipment);
        getEventBus().subscribe(EntityAddItemEvent.class, this::onEntityAddItemEvent);
        getEventBus().subscribe(UpdateQuestLogEvent.class, this::onQuestLogEvent);
        getEventBus().subscribe(PauseEvent.class, this::onPauseEvent);
        getEventBus().subscribe(OpenChestEvent.class, this::openChest);
        getEventBus().subscribe(CloseChestEvent.class, this::closeChest);
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

    @Override
    protected void onUiEvent(UiEvent event) {
        if (event.action() == UiEvent.Action.RELEASE) return;
        if (event.command() == Command.INVENTORY) {
            if (paused || isChestOpen) return;
            open = setOpen(!open, open, Constants.OPEN_INVENTORY);
        }
    }

    private void onAddItem(Array<Entity> items) {
        for (Entity itemEntity : items) {
            ItemModel model = uiModelFactory.createItemModel(itemEntity);
            if (model != null) {
                this.items.add(model);
            }
        }
    }

    private void rebuildInventory(Entity player) {
        Inventory inventory = Inventory.MAPPER.get(player);
        if (inventory == null) return;
        items.clear();
        onAddItem(inventory.getItems());
        propertyChangeSupport.firePropertyChange(Constants.ADD_ITEMS_TO_INVENTORY, null, items);
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
                if (paused)
                    closeInventory();
            }
        }
    }

    private void closeInventory() {
        if (!open) return;
        open = setOpen(false, open, Constants.OPEN_INVENTORY);
    }

    public void close() {
        closeInventory();
    }

    private void openChest(OpenChestEvent event) {
        isChestOpen = true;
    }

    private void closeChest(CloseChestEvent event) {
        isChestOpen = false;
    }

    @Override
    public void dispose() {
        getEventBus().unsubscribe(UpdateInventoryEvent.class, this::updateInventory);
        getEventBus().unsubscribe(UpdateEquipmentEvent.class, this::updateEquipment);
        getEventBus().unsubscribe(EntityAddItemEvent.class, this::onEntityAddItemEvent);
        getEventBus().unsubscribe(UpdateQuestLogEvent.class, this::onQuestLogEvent);
        getEventBus().unsubscribe(PauseEvent.class, this::onPauseEvent);
        getEventBus().unsubscribe(OpenChestEvent.class, this::openChest);
        getEventBus().unsubscribe(CloseChestEvent.class, this::closeChest);
    }
}
