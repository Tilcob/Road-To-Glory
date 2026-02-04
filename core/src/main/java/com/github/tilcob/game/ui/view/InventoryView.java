package com.github.tilcob.game.ui.view;

import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.Scaling;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.item.ItemCategory;
import com.github.tilcob.game.item.ItemModel;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.stat.StatType;
import com.github.tilcob.game.ui.inventory.*;
import com.github.tilcob.game.ui.inventory.equipment.EquipmentItemSource;
import com.github.tilcob.game.ui.inventory.equipment.EquipmentSlot;
import com.github.tilcob.game.ui.inventory.equipment.EquipmentSlotTarget;
import com.github.tilcob.game.ui.inventory.player.PlayerItemSource;
import com.github.tilcob.game.ui.inventory.player.PlayerSlot;
import com.github.tilcob.game.ui.inventory.player.PlayerSlotTarget;
import com.github.tilcob.game.ui.model.InventoryViewModel;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class InventoryView extends View<InventoryViewModel> {
    private Table inventoryContent;
    private Table questContent;
    private TextButton inventoryTabButton;
    private TextButton questTabButton;
    private PlayerSlot[][] slots;
    private EnumMap<ItemCategory, EquipmentSlot> equipmentSlots;
    private Label itemDetailsLabel;
    private Table statsTable;
    private ShiftClickListener shiftClickListener;
    private final IntSet occupiedPlayerSlots = new IntSet();
    private final QuestPanelController questPanelController;

    public InventoryView(Skin skin, Stage stage, InventoryViewModel viewModel) {
        super(skin, stage, viewModel);
        this.questPanelController = new QuestPanelController(skin);
    }

    @Override
    protected void setupUI() {
        setFillParent(true);
        slots = new PlayerSlot[Constants.INVENTORY_ROWS][Constants.INVENTORY_COLUMNS];
        equipmentSlots = new EnumMap<>(ItemCategory.class);
        dragAndDrop = new InventoryDragAndDrop();
        ShiftClickHandler shiftClickHandler = new ShiftClickHandler(viewModel.getEventBus());
        shiftClickListener = new ShiftClickListener(shiftClickHandler, buildShiftClickContext());

        Table inventoryRoot = new Table();
        inventoryRoot.setFillParent(true);
        setRoot(inventoryRoot);
        setVisibleBound(false);

        addActor(inventoryRoot);

        Table panelRoot = new Table();
        panelRoot.setBackground(skin.getDrawable("Other_panel_brown"));
        panelRoot.pad(10.0f);
        panelRoot.top();

        Table tabsTable = new Table();
        inventoryTabButton = new TextButton("Inventory", skin);
        questTabButton = new TextButton("Quests", skin);
        inventoryTabButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setActiveTab(InventoryTab.INVENTORY);
            }
        });
        questTabButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setActiveTab(InventoryTab.QUESTS);
            }
        });
        tabsTable.add(inventoryTabButton).padRight(6).height(32);
        tabsTable.add(questTabButton).height(32);

        Stack contentStack = new Stack();
        inventoryContent = buildInventoryContent();
        questContent = buildQuestContent();
        contentStack.add(inventoryContent);
        contentStack.add(questContent);

        panelRoot.add(tabsTable).left().row();
        panelRoot.add(contentStack).expand().fill().padTop(6.0f);

        inventoryRoot.clearChildren();
        inventoryRoot.add(panelRoot).center();

        setActiveTab(InventoryTab.INVENTORY);

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
        viewModel.onPropertyChange(Constants.UPDATE_STATS, Map.class, this::updateStats);
    }

    private void updateQuests(Array<Quest> quests) {
        questPanelController.updateQuests(quests);
    }

    private void updatePlayerItems(Array<ItemModel> array) {
        clearItemDetails();
        occupiedPlayerSlots.clear();
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
                attachShiftClick(itemImage, item, SlotContext.EQUIPPED);
                dragAndDrop.addSource(new EquipmentItemSource(itemImage, item.getSlotIdx(), item.getCategory()));
                continue;
            }

            int idx = item.getSlotIdx();
            int row = idx / Constants.INVENTORY_COLUMNS;
            int col = idx % Constants.INVENTORY_COLUMNS;
            if (row < 0 || row >= Constants.INVENTORY_ROWS || col < 0) continue;

            PlayerSlot slot = slots[row][col];
            Image itemImage = renderItemInSlot(item, slot);
            occupiedPlayerSlots.add(idx);
            attachShiftClick(itemImage, item, SlotContext.PLAYER_INVENTORY);

            dragAndDrop.addSource(new PlayerItemSource(itemImage, item.getSlotIdx()));
        }
    }

    private void setInventoryVisibility(boolean isVisible) {
        setVisibleBound(isVisible);
    }

    private void setActiveTab(InventoryTab tab) {
        boolean isInventory = tab == InventoryTab.INVENTORY;
        inventoryTabButton.setDisabled(isInventory);
        questTabButton.setDisabled(!isInventory);
        inventoryContent.setVisible(isInventory);
        inventoryContent.setTouchable(isInventory ? Touchable.enabled : Touchable.disabled);
        questContent.setVisible(!isInventory);
        questContent.setTouchable(!isInventory ? Touchable.enabled : Touchable.disabled);
    }

    private Table buildInventoryContent() {
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
                contentTable.add(slot).size(35, 35);
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

        Table detailsTable = new Table();
        detailsTable.setBackground(skin.getDrawable("Other_panel_brown"));
        Label detailsHeader = new Label("Item Details", skin, "text_12");
        detailsHeader.setColor(skin.getColor("BLACK"));
        detailsTable.add(detailsHeader).left().row();

        itemDetailsLabel = new Label("Hover an item to see details.", skin, "text_08");
        itemDetailsLabel.setColor(skin.getColor("BLACK"));
        itemDetailsLabel.setWrap(true);
        detailsTable.add(itemDetailsLabel).left().width(280).pad(4.0f);

        Table statsPanel = new Table();
        statsPanel.setBackground(skin.getDrawable("Other_panel_brown"));
        Label statsHeader = new Label("Stats", skin, "text_12");
        statsHeader.setColor(skin.getColor("BLACK"));
        statsPanel.add(statsHeader).left().row();

        statsTable = new Table();
        statsTable.defaults().left().padBottom(2.0f);
        statsPanel.add(statsTable).left().pad(4.0f);

        Table leftColumn = new Table();
        leftColumn.add(table1).left().row();
        leftColumn.add(detailsTable).left().padTop(6.0f);

        Table rightColumn = new Table();
        rightColumn.add(equipmentTable).left().row();
        rightColumn.add(statsPanel).left().padTop(6.0f);

        Table content = new Table();
        content.defaults().pad(6.0f);
        content.add(leftColumn).top();
        content.add(rightColumn).top();

        return content;
    }

    private Table buildQuestContent() {
        Table questListPanel = new Table();
        questListPanel.setBackground(skin.getDrawable("Other_panel_brown"));
        Label questLabel = new Label("Quests", skin, "text_12");
        questLabel.setColor(skin.getColor("BLACK"));
        questListPanel.add(questLabel).left().row();

        Table questLog = new Table();
        ScrollPane questScrollPane = new ScrollPane(questLog, skin);
        questScrollPane.setFadeScrollBars(false);
        questScrollPane.setScrollingDisabled(true, false);
        questScrollPane.setOverscroll(false, false);
        questListPanel.add(questScrollPane).expand().fill().padTop(4.0f);

        Table questStepsPanel = new Table();
        questStepsPanel.setBackground(skin.getDrawable("Other_panel_brown"));
        Label stepsLabel = new Label("Quest Steps", skin, "text_12");
        stepsLabel.setColor(skin.getColor("BLACK"));
        questStepsPanel.add(stepsLabel).left().row();

        Table questSteps = new Table();
        ScrollPane stepsScrollPane = new ScrollPane(questSteps, skin);
        stepsScrollPane.setFadeScrollBars(false);
        stepsScrollPane.setScrollingDisabled(true, false);
        stepsScrollPane.setOverscroll(false, false);
        questStepsPanel.add(stepsScrollPane).expand().fill().padTop(4.0f);

        Table content = new Table();
        content.defaults().pad(6.0f);
        content.add(questListPanel).top().expandY().fillY().width(260);
        content.add(questStepsPanel).top().expand().fill();
        questPanelController.bindTables(questLog, questSteps);
        return content;
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

    private void updateStats(Map<StatType, Float> stats) {
        if (statsTable == null) return;
        statsTable.clear();

        if (stats == null || stats.isEmpty()) {
            Label emptyLabel = new Label("No stats available.", skin, "text_08");
            emptyLabel.setColor(skin.getColor("BLACK"));
            statsTable.add(emptyLabel).left().row();
            return;
        }
        for (StatType type : StatType.values()) {
            Float value = stats.get(type);
            if (value == null) continue;
            String name = formatStatName(type);
            Label statLabel = new Label(name + ": " + String.format("%.0f", value), skin, "text_08");
            statLabel.setColor(skin.getColor("BLACK"));
            statsTable.add(statLabel).left().row();
        }
    }

    private String formatStatName(StatType type) {
        String raw = type.getId().replace("_", " ");
        String[] parts = raw.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) continue;
            if (!builder.isEmpty()) builder.append(' ');
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.toString();
    }

    private void showItemDetails(ItemModel item) {
        if (itemDetailsLabel == null || item == null) return;

        String name = item.getName();
        if (name == null || name.isBlank()) name = "Unknown Item";

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
        public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            showItemDetails(item);
        }

        @Override
        public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            clearItemDetails();
        }
    }

    private void attachShiftClick(Image itemImage, ItemModel item, SlotContext slotContext) {
        itemImage.setUserObject(new ShiftClickPayload(item, slotContext));
        itemImage.addListener(shiftClickListener);
    }

    private ShiftClickContext buildShiftClickContext() {
        return new ShiftClickContext() {
            @Override
            public boolean isChestOpen() {
                return viewModel.isChestOpen();
            }

            @Override
            public boolean canEquip(ItemModel item) {
                if (item == null || item.isEquipped()) {
                    return false;
                }
                return equipmentSlots.containsKey(item.getCategory());
            }

            @Override
            public int findEmptyPlayerSlot() {
                for (int i = 0; i < Constants.INVENTORY_CAPACITY; i++) {
                    if (!occupiedPlayerSlots.contains(i)) {
                        return i;
                    }
                }
                return -1;
            }
        };
    }

    private enum InventoryTab {
        INVENTORY,
        QUESTS
    }
}
