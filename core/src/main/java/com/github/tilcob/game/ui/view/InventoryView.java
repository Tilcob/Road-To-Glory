package com.github.tilcob.game.ui.view;

import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.item.ItemCategory;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.ui.inventory.InventoryDragAndDrop;
import com.github.tilcob.game.ui.inventory.equipment.EquipmentItemSource;
import com.github.tilcob.game.ui.inventory.equipment.EquipmentSlot;
import com.github.tilcob.game.ui.inventory.equipment.EquipmentSlotTarget;
import com.github.tilcob.game.ui.inventory.player.PlayerItemSource;
import com.github.tilcob.game.ui.inventory.player.PlayerSlot;
import com.github.tilcob.game.ui.inventory.player.PlayerSlotTarget;
import com.github.tilcob.game.ui.model.InventoryViewModel;
import com.github.tilcob.game.ui.model.ItemModel;

import java.util.EnumMap;

public class InventoryView extends View<InventoryViewModel> {
    private Table inventoryRoot;
    private PlayerSlot[][] slots;
    private EnumMap<ItemCategory, EquipmentSlot> equipmentSlots;
    private Table questLog;
    private Label itemDetailsLabel;

    public InventoryView(Skin skin, Stage stage, InventoryViewModel viewModel) {
        super(skin, stage, viewModel);
    }

    @Override
    protected void setupUI() {
        setFillParent(true);
        slots = new PlayerSlot[Constants.INVENTORY_ROWS][Constants.INVENTORY_COLUMNS];
        equipmentSlots = new EnumMap<>(ItemCategory.class);
        dragAndDrop = new InventoryDragAndDrop();

        inventoryRoot = new Table();
        inventoryRoot.setFillParent(true);
        inventoryRoot.setVisible(false);

        addActor(inventoryRoot);

        Table table1 = new Table();
        table1.setBackground(skin.getDrawable("Other_panel_brown"));
        Label label = new Label("Inventory", skin, "text_12");
        label.setColor(skin.getColor("BLACK"));
        table1.add(label).row();
        Table contentTable = new Table();

        for (int i = 0; i < Constants.INVENTORY_ROWS; i++) {
            for (int j = 0; j < Constants.INVENTORY_COLUMNS; j++) {
                int index = i * Constants.INVENTORY_COLUMNS + j;
                PlayerSlot slot = new PlayerSlot(index, skin, viewModel.getEventBus());
                this.slots[i][j] = slot;
                contentTable.add(slot).size(35,35);
            }
            contentTable.row();
        }
        table1.add(contentTable).pad(5.0f);

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
            EquipmentSlot equipmentSlot = new EquipmentSlot(skin, viewModel.getEventBus());
            equipmentSlots.put(category, equipmentSlot);
            equipmentGrid.add(buildEquipmentSlot(category, equipmentSlot)).size(35, 35).pad(2.0f);
            if ((i + 1) % columns == 0) {
                equipmentGrid.row();
            }
        }
        equipmentTable.add(equipmentGrid).pad(5.0f);

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

        Table detailsTable = new Table();
        detailsTable.setBackground(skin.getDrawable("Other_panel_brown"));
        Label detailsHeader = new Label("Item Details", skin, "text_12");
        detailsHeader.setColor(skin.getColor("BLACK"));
        detailsTable.add(detailsHeader).left().row();

        itemDetailsLabel = new Label("Hover an item to see details.", skin, "text_08");
        itemDetailsLabel.setColor(skin.getColor("BLACK"));
        itemDetailsLabel.setWrap(true);
        detailsTable.add(itemDetailsLabel).left().width(280).pad(4.0f);

        Table panelRoot = new Table();
        panelRoot.defaults().pad(5.0f);

        panelRoot.add(table1).top();
        panelRoot.add(equipmentTable).top();
        panelRoot.add(scrollTable).top().row();
        panelRoot.add(detailsTable).colspan(3).left();

        inventoryRoot.clearChildren();
        inventoryRoot.add(panelRoot).center();

        for (int i = 0; i < Constants.INVENTORY_ROWS; i++) {
            for (int j = 0; j < Constants.INVENTORY_COLUMNS; j++) {
                PlayerSlot slot = slots[i][j];
                dragAndDrop.addTarget(new PlayerSlotTarget(slot, viewModel.getEventBus()));
            }
        }

        for (ItemCategory category : equipmentSlots.keySet()) {
            EquipmentSlot slot = equipmentSlots.get(category);
            dragAndDrop.addTarget(new EquipmentSlotTarget(slot, category, viewModel.getEventBus()));
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
        clearItemDetails();
        for (int i = 0; i < Constants.INVENTORY_ROWS; i++) {
            for (int j = 0; j < Constants.INVENTORY_COLUMNS; j++) {
                clearSlot(slots[i][j]);
            }
        }

        for (EquipmentSlot equipmentSlot : equipmentSlots.values()) {
            clearSlot(equipmentSlot);
        }

        for (ItemModel item : array) {
            if (item.isEquipped()) {
                EquipmentSlot equipmentSlot = equipmentSlots.get(item.getCategory());
                if (equipmentSlot == null) continue;
                Image itemImage = renderItemInSlot(item, equipmentSlot);
                dragAndDrop.addSource(new EquipmentItemSource(itemImage, item.getSlotIdx(), item.getCategory()));
                continue;
            }

            int idx = item.getSlotIdx();
            int row = idx / Constants.INVENTORY_COLUMNS;
            int col = idx % Constants.INVENTORY_COLUMNS;
            if (row < 0 || row >= Constants.INVENTORY_ROWS || col < 0) continue;

            PlayerSlot slot = slots[row][col];
            Image itemImage = renderItemInSlot(item, slot);

            dragAndDrop.addSource(new PlayerItemSource(itemImage, item.getSlotIdx()));
        }
    }

    private void setInventoryVisibility(boolean isVisible) {
        inventoryRoot.setVisible(isVisible);
    }

    private Stack buildEquipmentSlot(ItemCategory category, EquipmentSlot slot) {
        Stack stack = new Stack();
        stack.add(slot);

        Label label = new Label(category.name(), skin, "text_08");
        label.setColor(skin.getColor("BLACK"));
        Table labelTable = new Table();
        labelTable.setFillParent(true);
        labelTable.setTouchable(Touchable.disabled);
        labelTable.add(label).center();
        stack.add(labelTable);

        return stack;
    }

    private void clearSlot(Stack slot) {
        Actor actor = slot.findActor("itemId");
        if (actor != null) actor.remove();
        if (slot instanceof PlayerSlot playerSlot) {
            playerSlot.setCount(0);
        }
    }

    private Image renderItemInSlot(ItemModel item, Stack slot) {
        Image itemImage = new Image(skin.getDrawable(item.getDrawableName()));
        itemImage.setName("itemId");
        itemImage.setScaling(Scaling.fit);
        itemImage.addListener(new ItemDetailsListener(item));

        slot.add(itemImage);
        if (slot instanceof PlayerSlot playerSlot) {
            playerSlot.setCount(item.getCount());
            playerSlot.getCountTable().toFront();
        }
        return itemImage;
    }

    private void clearItemDetails() {
        if (itemDetailsLabel != null) {
            itemDetailsLabel.setText("Hover an item to see details.");
        }
    }

    private void showItemDetails(ItemModel item) {
        if (itemDetailsLabel == null || item == null) {
            return;
        }
        String name = item.getName();
        if (name == null || name.isBlank()) {
            name = "Unknown Item";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(name);
        builder.append(" (").append(item.getCategory().name()).append(")");
        if (item.getCount() > 1) {
            builder.append(" x").append(item.getCount());
        }
        itemDetailsLabel.setText(builder.toString());
    }

    private class ItemDetailsListener extends InputListener {
        private final ItemModel item;

        private ItemDetailsListener(ItemModel item) {
            this.item = item;
        }

        @Override
        public void enter(InputEvent event, float x, float y, int pointer,
                          Actor fromActor) {
            showItemDetails(item);
        }

        @Override
        public void exit(InputEvent event, float x, float y, int pointer,
                         Actor toActor) {
            clearItemDetails();
        }
    }
}
