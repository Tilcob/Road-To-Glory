package com.github.tilcob.game.ui.model;

import com.github.tilcob.game.item.ItemCategory;

public class ItemModel {
    private final int itemEntityId;
    private final ItemCategory category;
    private final String name;
    private final String drawableName;
    private int slotIdx;
    private boolean equipped;
    private final int count;

    public ItemModel(int itemEntityId, ItemCategory category, String name, String drawableName, int slotIdx, boolean equipped, int count) {
        this.itemEntityId = itemEntityId;
        this.category = category;
        this.name = name;
        this.drawableName = drawableName;
        this.slotIdx = -1;
        this.equipped = false;
        this.slotIdx = slotIdx;
        this.equipped = equipped;
        this.count = count;
    }

    public int getItemEntityId() {
        return itemEntityId;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getDrawableName() {
        return drawableName;
    }

    public int getSlotIdx() {
        return slotIdx;
    }

    public boolean isEquipped() {
        return equipped;
    }

    public int getCount() {
        return count;
    }

    public void setSlotIdx(int slotIdx) {
        this.slotIdx = slotIdx;
    }

    public void setEquipped(boolean equipped) {
        this.equipped = equipped;
    }
}
