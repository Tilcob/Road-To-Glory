package com.github.tilcob.game.ui.inventory.equipment;

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.github.tilcob.game.event.EquipItemEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.item.ItemCategory;
import com.github.tilcob.game.ui.inventory.chest.ChestItemSource;
import com.github.tilcob.game.ui.inventory.chest.ChestSlotTarget;
import com.github.tilcob.game.ui.inventory.player.PlayerItemSource;

public class EquipmentSlotTarget extends DragAndDrop.Target {
    private final ItemCategory category;
    private final GameEventBus eventBus;

    public EquipmentSlotTarget(EquipmentSlot slot, ItemCategory category, GameEventBus eventBus) {
        super(slot);
        this.category = category;
        this.eventBus = eventBus;
    }

    @Override
    public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
        return payload.getObject() instanceof PlayerItemSource
            || payload.getObject() instanceof ChestItemSource;
    }

    @Override
    public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
        Object payloadObject = payload.getObject();
        if (payloadObject instanceof PlayerItemSource playerItemSource) {
            eventBus.fire(new EquipItemEvent(category, playerItemSource.getFromIdx()));
        }
        if (payloadObject instanceof ChestItemSource chestItemSource) {
            eventBus.fire(new EquipItemEvent(category, chestItemSource.getFromIdx()));
        }

    }
}
