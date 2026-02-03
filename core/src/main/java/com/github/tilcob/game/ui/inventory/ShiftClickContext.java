package com.github.tilcob.game.ui.inventory;

import com.github.tilcob.game.item.ItemModel;

public interface ShiftClickContext {
    boolean isChestOpen();

    boolean canEquip(ItemModel item);

    int findEmptyPlayerSlot();
}
