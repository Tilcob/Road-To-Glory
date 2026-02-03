package com.github.tilcob.game.ui.inventory;

import com.github.tilcob.game.item.ItemModel;

public record ShiftClickPayload(ItemModel item, SlotContext sourceContext) {
}
