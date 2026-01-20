package com.github.tilcob.game.ui.inventory;

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.github.tilcob.game.event.EquipItemEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.item.ItemCategory;

public class EquipmentSlotTarget extends DragAndDrop.Target {
    private final ItemCategory category;
    private final GameEventBus eventBus;

    public EquipmentSlotTarget(InventorySlot slot, ItemCategory category, GameEventBus eventBus) {
        super(slot);
        this.category = category;
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
        eventBus.fire(new EquipItemEvent(category, fromIdx));
    }
}
