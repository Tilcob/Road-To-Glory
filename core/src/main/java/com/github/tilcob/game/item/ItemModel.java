package com.github.tilcob.game.item;

import com.github.tilcob.game.stat.StatType;

import java.util.HashMap;
import java.util.Map;

public class ItemModel {
    private final int itemEntityId;
    private final ItemCategory category;
    private final String name;
    private final String description;
    private final String drawableName;
    private final Map<StatType, Float> requirements;
    private int slotIdx;
    private boolean equipped;
    private final int count;

    public ItemModel(int itemEntityId, ItemCategory category, String name, String description,
                     Map<StatType, Float> requirements, String drawableName, int slotIdx,
                     boolean equipped, int count) {
        this.itemEntityId = itemEntityId;
        this.category = category;
        this.name = name;
        this.description = description;
        this.drawableName = drawableName;
        this.slotIdx = -1;
        this.equipped = false;
        this.slotIdx = slotIdx;
        this.requirements = requirements == null ? Map.of() : Map.copyOf(requirements);
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

    public String getDescription() {
        return description;
    }

    public String getDrawableName() {
        return drawableName;
    }

    public Map<StatType, Float> getRequirements() {
        return requirements;
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
