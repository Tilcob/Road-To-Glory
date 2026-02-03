package com.github.tilcob.game.ui.inventory;

import com.github.tilcob.game.entity.TransferPlayerToChestAutoEvent;
import com.github.tilcob.game.event.EquipItemEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.TransferChestToPlayerAutoEvent;
import com.github.tilcob.game.event.UnequipItemEvent;
import com.github.tilcob.game.item.ItemModel;

public class ShiftClickHandler {
    private final GameEventBus eventBus;

    public ShiftClickHandler(GameEventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void handleShiftClick(ItemModel item, SlotContext sourceContext, ShiftClickContext context) {
        if (item == null || sourceContext == null || context == null) {
            return;
        }
        SlotContext target = getShiftClickTarget(sourceContext, item, context);
        if (target == null) {
            return;
        }

        switch (target) {
            case PLAYER_INVENTORY -> {
                if (sourceContext == SlotContext.CHEST) {
                    eventBus.fire(new TransferChestToPlayerAutoEvent(item.getSlotIdx()));
                } else if (sourceContext == SlotContext.EQUIPPED) {
                    int emptySlot = context.findEmptyPlayerSlot();
                    if (emptySlot == -1) {
                        return;
                    }
                    eventBus.fire(new UnequipItemEvent(item.getCategory(), emptySlot));
                }
            }
            case CHEST -> eventBus.fire(new TransferPlayerToChestAutoEvent(item.getSlotIdx()));
            case EQUIPPED -> eventBus.fire(new EquipItemEvent(item.getCategory(), item.getSlotIdx()));
        }
    }

    public SlotContext getShiftClickTarget(SlotContext sourceContext, ItemModel item, ShiftClickContext context) {
        if (sourceContext == null || item == null || context == null) {
            return null;
        }

        return switch (sourceContext) {
            case CHEST -> SlotContext.PLAYER_INVENTORY;
            case EQUIPPED -> SlotContext.PLAYER_INVENTORY;
            case PLAYER_INVENTORY -> {
                if (context.canEquip(item)) {
                    yield SlotContext.EQUIPPED;
                }
                if (context.isChestOpen()) {
                    yield SlotContext.CHEST;
                }
                yield null;
            }
        };
    }
}
