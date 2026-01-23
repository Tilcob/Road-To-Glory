package com.github.tilcob.game.ui.view;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.ui.inventory.InventoryDragAndDrop;
import com.github.tilcob.game.ui.inventory.chest.ChestSlot;
import com.github.tilcob.game.ui.model.ChestInventoryViewModel;

public class ChestView extends View<ChestInventoryViewModel> {
    private Table inventoryRoot;
    private ChestSlot[][] slots;

    public ChestView(Skin skin, Stage stage, ChestInventoryViewModel viewModel) {
        super(skin, stage, viewModel);
    }

    @Override
    protected void setupUI() {
        slots = new ChestSlot[Constants.INVENTORY_ROWS][Constants.INVENTORY_COLUMNS];
        dragAndDrop = new InventoryDragAndDrop();

        inventoryRoot = new Table();
        inventoryRoot.setFillParent(true);
        inventoryRoot.setVisible(false);

        Table table1 = new Table();
        table1.setBackground(skin.getDrawable("Other_panel_brown"));
        Label label = new Label("Inventory", skin, "text_12");
        label.setColor(skin.getColor("BLACK"));
        table1.add(label).row();
        Table contentTable = new Table();

        for (int i = 0; i < Constants.INVENTORY_ROWS; i++) {
            for (int j = 0; j < Constants.INVENTORY_COLUMNS; j++) {
                int index = i * Constants.INVENTORY_COLUMNS + j;
                ChestSlot slot = new ChestSlot(index, skin, viewModel.getEventBus());
                this.slots[i][j] = slot;
                contentTable.add(slot).size(35,35);
            }
            contentTable.row();
        }
        table1.add(contentTable).pad(5.0f);
        inventoryRoot.add(table1);
        stage.addActor(inventoryRoot);
    }

    @Override
    protected void setupPropertyChanges() {
        viewModel.onPropertyChange(Constants.OPEN_CHEST_INVENTORY, Boolean.class, this::setChestInventoryVisibility);
    }

    private void setChestInventoryVisibility(Boolean isVisible) {
        inventoryRoot.setVisible(isVisible);
    }
}
