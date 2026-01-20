package com.github.tilcob.game.ui.inventory;

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.github.tilcob.game.event.DragAndDropEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.UnequipItemEvent;

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
        return payload.getObject() instanceof InventoryItemSource
            || payload.getObject() instanceof EquipmentItemSource;
    }

    @Override
    public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
        Object payloadObject = payload.getObject();
        int toIdx = inventorySlot.getSlotIndex();
        if (payloadObject instanceof EquipmentItemSource equipmentSource) {
            eventBus.fire(new UnequipItemEvent(equipmentSource.getCategory(), toIdx));
            return;
        }
        if (payloadObject instanceof InventoryItemSource inventorySource) {
            int fromIdx = inventorySource.getFromIdx();
            eventBus.fire(new DragAndDropEvent(fromIdx, toIdx));
        }
    }
}
