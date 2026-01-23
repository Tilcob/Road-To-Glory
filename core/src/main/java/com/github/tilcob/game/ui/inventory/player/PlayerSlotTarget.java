package com.github.tilcob.game.ui.inventory.player;

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.github.tilcob.game.event.DragAndDropPlayerEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.UnequipItemEvent;
import com.github.tilcob.game.ui.inventory.chest.ChestItemSource;
import com.github.tilcob.game.ui.inventory.equipment.EquipmentItemSource;

public class PlayerSlotTarget extends DragAndDrop.Target {
    private final PlayerSlot playerSlot;
    private final GameEventBus eventBus;

    public PlayerSlotTarget(PlayerSlot slot, GameEventBus eventBus) {
        super(slot);
        this.playerSlot = slot;
        this.eventBus = eventBus;
    }

    @Override
    public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
        return payload.getObject() instanceof PlayerItemSource
            || payload.getObject() instanceof EquipmentItemSource
            || payload.getObject() instanceof ChestItemSource;
    }

    @Override
    public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
        Object payloadObject = payload.getObject();
        int toIdx = playerSlot.getSlotIndex();

        if (payloadObject instanceof EquipmentItemSource equipmentSource) {
            eventBus.fire(new UnequipItemEvent(equipmentSource.getCategory(), toIdx));
        } else if (payloadObject instanceof PlayerItemSource inventorySource) {
            int fromIdx = inventorySource.getFromIdx();
            eventBus.fire(new DragAndDropPlayerEvent(fromIdx, toIdx));
        } else if (payloadObject instanceof ChestItemSource chestSource) {
            int fromIdx = chestSource.getFromIdx();
            // TODO: Get item from chest
        }
    }
}
