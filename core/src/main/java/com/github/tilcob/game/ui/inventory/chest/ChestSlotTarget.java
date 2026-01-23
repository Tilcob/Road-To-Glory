package com.github.tilcob.game.ui.inventory.chest;

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.TransferChestToPlayerEvent;
import com.github.tilcob.game.event.TransferPlayerToChestEvent;
import com.github.tilcob.game.ui.inventory.equipment.EquipmentItemSource;
import com.github.tilcob.game.ui.inventory.player.PlayerItemSource;

public class ChestSlotTarget extends DragAndDrop.Target {
    private final ChestSlot chestSlot;
    private final GameEventBus eventBus;

    public ChestSlotTarget(ChestSlot chestSlot, GameEventBus eventBus) {
        super(chestSlot);
        this.chestSlot = chestSlot;
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
        int toIdx = chestSlot.getSlotIndex();

        if (payloadObject instanceof PlayerItemSource playerItemSource) {
            eventBus.fire(new TransferPlayerToChestEvent(playerItemSource.getFromIdx(), toIdx));
        } else if (payloadObject instanceof EquipmentItemSource equipmentItemSource) {
            eventBus.fire(new TransferPlayerToChestEvent(equipmentItemSource.getFromIdx(), toIdx));
        }
    }
}
