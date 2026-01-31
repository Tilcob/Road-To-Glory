package com.github.tilcob.game.ui.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.ui.inventory.InventoryDragAndDrop;
import com.github.tilcob.game.ui.inventory.chest.ChestItemSource;
import com.github.tilcob.game.ui.inventory.chest.ChestSlot;
import com.github.tilcob.game.ui.inventory.chest.ChestSlotTarget;
import com.github.tilcob.game.ui.inventory.player.PlayerItemSource;
import com.github.tilcob.game.ui.inventory.player.PlayerSlot;
import com.github.tilcob.game.ui.inventory.player.PlayerSlotTarget;
import com.github.tilcob.game.ui.model.ChestViewModel;
import com.github.tilcob.game.item.ItemModel;

public class ChestView extends View<ChestViewModel> {
    private Table inventoryRoot;
    private ChestSlot[][] chestSlots;
    private PlayerSlot[][] playerSlots;

    public ChestView(Skin skin, Stage stage, ChestViewModel viewModel) {
        super(skin, stage, viewModel);
    }

    @Override
    protected void setupUI() {
        setFillParent(true);
        chestSlots = new ChestSlot[Constants.INVENTORY_ROWS][Constants.INVENTORY_COLUMNS];
        playerSlots = new PlayerSlot[Constants.INVENTORY_ROWS][Constants.INVENTORY_COLUMNS];
        dragAndDrop = new InventoryDragAndDrop();

        inventoryRoot = new Table();
        inventoryRoot.setFillParent(true);
        inventoryRoot.setVisible(false);

        addActor(inventoryRoot);

        Table chestTable = new Table();
        chestTable.setBackground(skin.getDrawable("Other_panel_brown"));
        Label label = new Label("Chest", skin, "text_12");
        label.setColor(skin.getColor("BLACK"));
        chestTable.add(label).row();
        Table contentTable = new Table();

        for (int i = 0; i < Constants.INVENTORY_ROWS; i++) {
            for (int j = 0; j < Constants.INVENTORY_COLUMNS; j++) {
                int index = i * Constants.INVENTORY_COLUMNS + j;
                ChestSlot slot = new ChestSlot(index, skin, viewModel.getEventBus());
                this.chestSlots[i][j] = slot;
                contentTable.add(slot).size(35, 35);
            }
            contentTable.row();
        }
        chestTable.add(contentTable).pad(5.0f);

        Table playerTable = new Table();
        playerTable.setBackground(skin.getDrawable("Other_panel_brown"));
        Label playerLabel = new Label("Inventory", skin, "text_12");
        playerLabel.setColor(skin.getColor("BLACK"));
        playerTable.add(playerLabel).row();

        Table playerContentTable = new Table();
        for (int i = 0; i < Constants.INVENTORY_ROWS; i++) {
            for (int j = 0; j < Constants.INVENTORY_COLUMNS; j++) {
                int index = i * Constants.INVENTORY_COLUMNS + j;
                PlayerSlot slot = new PlayerSlot(index, skin, viewModel.getEventBus());
                this.playerSlots[i][j] = slot;
                playerContentTable.add(slot).size(35, 35);
            }
            playerContentTable.row();
        }
        playerTable.add(playerContentTable).pad(5.0f);

        Table panelRoot = new Table();
        panelRoot.defaults().pad(5.0f);
        panelRoot.add(chestTable).row();
        panelRoot.add(playerTable);

        inventoryRoot.clearChildren();
        inventoryRoot.add(panelRoot).expand().center();

        for (int i = 0; i < Constants.INVENTORY_ROWS; i++) {
            for (int j = 0; j < Constants.INVENTORY_COLUMNS; j++) {
                ChestSlot slot = chestSlots[i][j];
                dragAndDrop.addTarget(new ChestSlotTarget(slot, viewModel.getEventBus()));
                PlayerSlot playerSlot = playerSlots[i][j];
                dragAndDrop.addTarget(new PlayerSlotTarget(playerSlot, viewModel.getEventBus()));
            }
        }
    }

    @Override
    protected void setupPropertyChanges() {
        viewModel.onPropertyChange(Constants.OPEN_CHEST_INVENTORY, Boolean.class, this::setChestInventoryVisibility);
        viewModel.onPropertyChange(Constants.ADD_ITEMS_TO_CHEST, Array.class, this::updateChestItems);
        viewModel.onPropertyChange(Constants.ADD_ITEMS_TO_PLAYER_IN_CHEST, Array.class, this::updatePlayerItems);
    }

    private void updateChestItems(Array<ItemModel> chestItems) {
        clearItems(chestSlots);
        for (ItemModel item : chestItems) {
            int idx = item.getSlotIdx();
            int row = idx / Constants.INVENTORY_COLUMNS;
            int col = idx % Constants.INVENTORY_COLUMNS;
            if (row < 0 || row >= Constants.INVENTORY_ROWS || col < 0) continue;

            ChestSlot slot = chestSlots[row][col];
            Image itemImage = renderItemInSlot(item, slot);
            dragAndDrop.addSource(new ChestItemSource(itemImage, item.getSlotIdx()));
        }
    }

    private void updatePlayerItems(Array<ItemModel> playerItems) {
        clearItems(playerSlots);
        for (ItemModel item : playerItems) {
            if (item.isEquipped()) {
                continue;
            }
            int idx = item.getSlotIdx();
            int row = idx / Constants.INVENTORY_COLUMNS;
            int col = idx % Constants.INVENTORY_COLUMNS;
            if (row < 0 || row >= Constants.INVENTORY_ROWS || col < 0) continue;

            PlayerSlot slot = playerSlots[row][col];
            Image itemImage = renderItemInSlot(item, slot);
            dragAndDrop.addSource(new PlayerItemSource(itemImage, item.getSlotIdx()));
        }
    }

    private void setChestInventoryVisibility(Boolean isVisible) {
        inventoryRoot.setVisible(isVisible);
    }

    private void clearItems(Stack[][] slots) {
        for (int i = 0; i < Constants.INVENTORY_ROWS; i++) {
            for (int j = 0; j < Constants.INVENTORY_COLUMNS; j++) {
                clearSlot(slots[i][j]);
            }
        }
    }

    private void clearSlot(Stack slot) {
        Actor actor = slot.findActor("itemId");
        if (actor != null) actor.remove();
        if (slot instanceof ChestSlot chestSlot) {
            chestSlot.setCount(0);
        }
        if (slot instanceof PlayerSlot playerSlot) {
            playerSlot.setCount(0);
        }
    }

    private Image renderItemInSlot(ItemModel item, Stack slot) {
        Image itemImage = new Image(skin.getDrawable(item.getDrawableName()));
        itemImage.setName("itemId");
        itemImage.setScaling(Scaling.fit);

        slot.add(itemImage);
        if (slot instanceof ChestSlot chestSlot) {
            chestSlot.setCount(item.getCount());
            chestSlot.getCountTable().toFront();
        }
        if (slot instanceof PlayerSlot playerSlot) {
            playerSlot.setCount(item.getCount());
            playerSlot.getCountTable().toFront();
        }
        return itemImage;
    }
}
