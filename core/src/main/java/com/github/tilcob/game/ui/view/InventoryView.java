package com.github.tilcob.game.ui.view;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.item.ItemCategory;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.ui.inventory.InventoryDragAndDrop;
import com.github.tilcob.game.ui.inventory.InventoryItemSource;
import com.github.tilcob.game.ui.inventory.InventorySlot;
import com.github.tilcob.game.ui.inventory.InventorySlotTarget;
import com.github.tilcob.game.ui.model.InventoryViewModel;
import com.github.tilcob.game.ui.model.ItemModel;

import java.util.EnumMap;

public class InventoryView extends View<InventoryViewModel> {
    private Table inventoryRoot;
    private InventorySlot[][] slots;
    private EnumMap<ItemCategory, InventorySlot> equipmentSlots;
    private Table questLog;

    public InventoryView(Skin skin, Stage stage, InventoryViewModel viewModel) {
        super(skin, stage, viewModel);
    }

    @Override
    protected void setupUI() {
        slots = new InventorySlot[Constants.INVENTORY_ROWS][Constants.INVENTORY_COLUMNS];
        equipmentSlots = new EnumMap<>(ItemCategory.class);
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
                InventorySlot slot = new InventorySlot(index, skin, viewModel.getEventBus());
                this.slots[i][j] = slot;
                contentTable.add(slot).size(35,35);
            }
            contentTable.row();
        }
        table1.add(contentTable).pad(5.0f);
        inventoryRoot.add(table1);
        stage.addActor(inventoryRoot);

        Table equipmentTable = new Table();
        equipmentTable.setBackground(skin.getDrawable("Other_panel_brown"));
        Label equipmentLabel = new Label("Equipment", skin, "text_12");
        equipmentLabel.setColor(skin.getColor("BLACK"));
        equipmentTable.add(equipmentLabel).row();
        Table equipmentGrid = new Table();
        ItemCategory[] equipmentCategories = {
            ItemCategory.HELMET,
            ItemCategory.ARMOR,
            ItemCategory.WEAPON,
            ItemCategory.SHIELD,
            ItemCategory.BOOTS,
            ItemCategory.NECKLACE,
            ItemCategory.BRACELET,
            ItemCategory.RING
        };
        int columns = 4;
        for (int i = 0; i < equipmentCategories.length; i++) {
            ItemCategory category = equipmentCategories[i];
            InventorySlot equipmentSlot = new InventorySlot(-1, skin, viewModel.getEventBus());
            equipmentSlots.put(category, equipmentSlot);
            equipmentGrid.add(buildEquipmentSlot(category, equipmentSlot)).size(35, 35).pad(2.0f);
            if ((i + 1) % columns == 0) {
                equipmentGrid.row();
            }
        }
        equipmentTable.add(equipmentGrid).pad(5.0f);
        inventoryRoot.add(equipmentTable).pad(5.0f);

        Table scrollTable = new Table();
        scrollTable.setBackground(skin.getDrawable("Other_panel_brown"));
        Label questLabel = new Label("Quest Log", skin, "text_12");
        questLabel.setColor(skin.getColor("BLACK"));
        scrollTable.add(questLabel).row();
        questLog = new Table();
        ScrollPane scrollPane = new ScrollPane(questLog, skin);
        scrollPane.setHeight(inventoryRoot.getHeight());
        scrollPane.setScrollingDisabled(true, false);
        scrollTable.add(scrollPane);
        inventoryRoot.add(scrollTable).pad(5.0f);

        for (int i = 0; i < Constants.INVENTORY_ROWS; i++) {
            for (int j = 0; j < Constants.INVENTORY_COLUMNS; j++) {
                InventorySlot slot = slots[i][j];
                dragAndDrop.addTarget(new InventorySlotTarget(slot, viewModel.getEventBus()));
            }
        }
    }

    @Override
    protected void setupPropertyChanges() {
        viewModel.onPropertyChange(Constants.ADD_ITEMS_TO_INVENTORY, Array.class, this::updatePlayerItems);
        viewModel.onPropertyChange(Constants.OPEN_INVENTORY, Boolean.class, this::setInventoryVisibility);
        viewModel.onPropertyChange(Constants.ADD_QUESTS, Array.class, this::updateQuests);
    }

    private void updateQuests(Array<Quest> quests) {
        questLog.clear();
        for (Quest quest : quests) {
            String questTitle = quest.getTitle();
            if (questTitle == null || questTitle.isBlank()) {
                questTitle = quest.getQuestId().replace("_", " ");
            }
            Label label = new Label(questTitle, skin, "text_08");
            label.setColor(skin.getColor("BLACK"));
            Image image = new Image(skin.getDrawable("Green_icon_outline_checkmark"));
            image.setVisible(quest.isCompleted());
            questLog.add(label);
            questLog.add(image).row();
        }
    }

    private void updatePlayerItems(Array<ItemModel> array) {
        for (int i = 0; i < Constants.INVENTORY_ROWS; i++) {
            for (int j = 0; j < Constants.INVENTORY_COLUMNS; j++) {
                clearSlot(slots[i][j]);
            }
        }

        for (InventorySlot equipmentSlot : equipmentSlots.values()) {
            clearSlot(equipmentSlot);
        }

        for (ItemModel item : array) {
            if (item.isEquipped()) {
                InventorySlot equipmentSlot = equipmentSlots.get(item.getCategory());
                if (equipmentSlot == null) continue;
                renderItemInSlot(item, equipmentSlot);
                continue;
            }

            int idx = item.getSlotIdx();
            int row = idx / Constants.INVENTORY_COLUMNS;
            int col = idx % Constants.INVENTORY_COLUMNS;
            if (row < 0 || row >= Constants.INVENTORY_ROWS || col < 0) continue;

            InventorySlot slot = slots[row][col];
            Image itemImage = renderItemInSlot(item, slot);

            dragAndDrop.addSource(new InventoryItemSource(itemImage, item.getSlotIdx(), viewModel.getEventBus()));
        }
    }

    private void setInventoryVisibility(boolean isVisible) {
        inventoryRoot.setVisible(isVisible);
    }

    private Stack buildEquipmentSlot(ItemCategory category, InventorySlot slot) {
        Stack stack = new Stack();
        stack.add(slot);

        Label label = new Label(category.name(), skin, "text_08");
        label.setColor(skin.getColor("BLACK"));
        Table labelTable = new Table();
        labelTable.setFillParent(true);
        labelTable.add(label).center();
        stack.add(labelTable);

        return stack;
    }

    private void clearSlot(InventorySlot slot) {
        Actor actor = slot.findActor("itemId");
        if (actor != null) actor.remove();
        slot.setCount(0);
    }

    private Image renderItemInSlot(ItemModel item, InventorySlot slot) {
        Image itemImage = new Image(skin.getDrawable(item.getDrawableName()));
        itemImage.setName("itemId");
        itemImage.setScaling(Scaling.fit);

        slot.add(itemImage);
        slot.setCount(item.getCount());
        slot.getCountTable().toFront();
        return itemImage;
    }
}
