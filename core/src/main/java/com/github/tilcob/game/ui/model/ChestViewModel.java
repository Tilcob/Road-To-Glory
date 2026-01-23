package com.github.tilcob.game.ui.model;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.component.Chest;
import com.github.tilcob.game.component.Id;
import com.github.tilcob.game.component.Inventory;
import com.github.tilcob.game.component.Item;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.*;
import com.github.tilcob.game.item.ItemDefinition;
import com.github.tilcob.game.item.ItemDefinitionRegistry;

public class ChestViewModel extends ViewModel {
    private final Array<ItemModel> chestItems = new Array<>();
    private final Array<ItemModel> playerItems = new Array<>();
    private boolean open = false;
    private boolean paused = false;
    private Entity currentChest;
    private Entity currentPlayer;

    public ChestViewModel(GameServices services) {
        super(services);

        getEventBus().subscribe(OpenChestEvent.class, this::onOpenChest);
        getEventBus().subscribe(UpdateChestInventoryEvent.class, this::onChestInventoryUpdate);
        getEventBus().subscribe(UpdateInventoryEvent.class, this::onPlayerInventoryUpdate);
        getEventBus().subscribe(CloseChestEvent.class, this::onCloseChest);
        getEventBus().subscribe(PauseEvent.class, this::onPauseEvent);
    }

    private void onOpenChest(OpenChestEvent event) {
        if (paused) return;
        open = true;
        currentChest = event.chestEntity();
        currentPlayer = event.player();
        rebuildChestItems();
        rebuildPlayerItems();
        this.propertyChangeSupport.firePropertyChange(Constants.OPEN_CHEST_INVENTORY, null, true);
    }

    private void onChestInventoryUpdate(UpdateChestInventoryEvent event) {
        if (!open || event.player() != currentPlayer) return;
        rebuildChestItems();
    }

    private void onPlayerInventoryUpdate(UpdateInventoryEvent event) {
        if (!open || event.player() != currentPlayer) return;
        rebuildPlayerItems();
    }

    private void onCloseChest(CloseChestEvent event) {
        if (!open) return;
        if (event.chest() != null && Chest.MAPPER.get(currentChest) != event.chest()) return;
        open = false;
        currentChest = null;
        currentPlayer = null;
        this.propertyChangeSupport.firePropertyChange(Constants.OPEN_CHEST_INVENTORY, true, false);
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
        currentChest = null;
        currentPlayer = null;
        this.propertyChangeSupport.firePropertyChange(Constants.OPEN_CHEST_INVENTORY, old, false);
    }

    private void rebuildChestItems() {
        chestItems.clear();
        if (currentChest == null) return;
        Chest chest = Chest.MAPPER.get(currentChest);
        if (chest == null) return;
        Array<String> contents = chest.getContents();
        for (int i = 0; i < contents.size; i++) {
            String itemId = ItemDefinitionRegistry.resolveId(contents.get(i));
            ItemDefinition definition = ItemDefinitionRegistry.get(itemId);
            chestItems.add(new ItemModel(
                -1,
                definition.category(),
                definition.name(),
                definition.icon(),
                i,
                false,
                1
            ));
        }
        propertyChangeSupport.firePropertyChange(Constants.ADD_ITEMS_TO_CHEST, null, chestItems);
    }

    private void rebuildPlayerItems() {
        playerItems.clear();
        if (currentPlayer == null) return;
        Inventory inventory = Inventory.MAPPER.get(currentPlayer);
        if (inventory == null) return;
        for (Entity itemEntity : inventory.getItems()) {
            Item item = Item.MAPPER.get(itemEntity);
            Id idComp = Id.MAPPER.get(itemEntity);
            if (idComp == null) continue;

            ItemDefinition definition = ItemDefinitionRegistry.get(item.getItemId());
            playerItems.add(new ItemModel(
                idComp.getId(),
                definition.category(),
                definition.name(),
                definition.icon(),
                item.getSlotIndex(),
                item.isEquipped(),
                item.getCount()
            ));
        }
        propertyChangeSupport.firePropertyChange(Constants.ADD_ITEMS_TO_PLAYER_IN_CHEST, null, playerItems);
    }

    @Override
    public void dispose() {
        getEventBus().unsubscribe(OpenChestEvent.class, this::onOpenChest);
        getEventBus().unsubscribe(UpdateChestInventoryEvent.class, this::onChestInventoryUpdate);
        getEventBus().unsubscribe(UpdateInventoryEvent.class, this::onPlayerInventoryUpdate);
        getEventBus().unsubscribe(CloseChestEvent.class, this::onCloseChest);
        getEventBus().unsubscribe(PauseEvent.class, this::onPauseEvent);
    }
}
