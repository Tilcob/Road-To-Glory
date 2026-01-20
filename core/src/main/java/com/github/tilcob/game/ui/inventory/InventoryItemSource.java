package com.github.tilcob.game.ui.inventory;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.InventoryDropEvent;


public class InventoryItemSource extends DragAndDrop.Source {
    private final Image image;
    private final int fromIdx;
    private final GameEventBus eventBus;

    public InventoryItemSource(Image image, int fromIdx, GameEventBus eventBus) {
        super(image);
        this.image = image;
        this.fromIdx = fromIdx;
        this.eventBus = eventBus;
    }

    @Override
    public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
        DragAndDrop.Payload payload = new DragAndDrop.Payload();
        payload.setObject(this);
        payload.setDragActor(new Image(image.getDrawable()));
        return payload;
    }

    @Override
    public void dragStop(InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target) {
        if (target != null) return;
        eventBus.fire(new InventoryDropEvent(fromIdx));
    }

    public Image getImage() {
        return image;
    }

    public int getFromIdx() {
        return fromIdx;
    }
}
