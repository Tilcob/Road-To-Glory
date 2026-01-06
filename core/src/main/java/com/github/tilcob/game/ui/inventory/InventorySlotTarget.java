package com.github.tilcob.game.ui.inventory;

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.github.tilcob.game.event.DragAndDropEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.ui.model.ItemModel;

public class InventorySlotTarget extends DragAndDrop.Target {
    private final InventorySlot inventorySlot;
    private final GameEventBus eventBus;

    public InventorySlotTarget(InventorySlot slot, GameEventBus eventBus) {
        super(slot);
        this.inventorySlot = slot;
        this.eventBus = eventBus;
    }

    @Override
    public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
        return true;
    }

    @Override
    public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
        InventoryItemSource itemSource = (InventoryItemSource) payload.getObject();
        int fromIdx = itemSource.getFromIdx();
        int toIdx = inventorySlot.getSlotIndex();
        eventBus.fire(new DragAndDropEvent(fromIdx, toIdx));
    }
}
