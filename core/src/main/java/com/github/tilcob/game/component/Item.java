package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class Item implements Component {
    public static final ComponentMapper<Item> MAPPER = ComponentMapper.getFor(Item.class);

    private final String itemId;
    private int slotIndex;
    private boolean equipped;
    private int count;

    public Item(String itemId, int slotIndex, int count) {
        this.itemId = itemId;
        this.slotIndex = slotIndex;
        this.equipped = false;
        this.count = count;
    }

    public String getItemId() {
        return itemId;
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
