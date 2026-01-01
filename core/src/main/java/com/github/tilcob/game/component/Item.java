package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.github.tilcob.game.item.ItemType;

public class Item implements Component {
    public static final ComponentMapper<Item> MAPPER = ComponentMapper.getFor(Item.class);

    private final ItemType itemType;
    private int slotIndex;
    private boolean equipped;

    public Item(ItemType itemType, int slotIndex) {
        this.itemType = itemType;
        this.slotIndex = slotIndex;
        this.equipped = false;
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

    public void setEquipped(boolean equipped) {
        this.equipped = equipped;
    }
}
