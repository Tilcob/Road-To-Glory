package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.github.tilcob.game.item.ItemType;

public class Item implements Component {
    public static final ComponentMapper<Item> MAPPER = ComponentMapper.getFor(Item.class);

    private final ItemType itemType;
    private int slotIndex;
    private boolean equipped;
    private int count;

    public Item(ItemType itemType, int slotIndex, int count) {
        this.itemType = itemType;
        this.slotIndex = slotIndex;
        this.equipped = false;
        this.count = count;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public void setSlotIndex(int slotIndex) {
        this.slotIndex = slotIndex;
    }

    public boolean isEquipped() {
        return equipped;
    }

    public int getCount() {
        return count;
    }

    public void setEquipped(boolean equipped) {
        this.equipped = equipped;
    }

    public void add(int amount) {
        count += amount;
    }

    public void remove(int amount) {
        count -= amount;
    }
}
