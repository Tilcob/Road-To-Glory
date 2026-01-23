package com.github.tilcob.game.ui.inventory.player;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;


public class PlayerItemSource extends DragAndDrop.Source {
    private final Image image;
    private final int fromIdx;

    public PlayerItemSource(Image image, int fromIdx) {
        super(image);
        this.image = image;
        this.fromIdx = fromIdx;
    }

    @Override
    public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
        DragAndDrop.Payload payload = new DragAndDrop.Payload();
        payload.setObject(this);
        payload.setDragActor(new Image(image.getDrawable()));
        return payload;
    }

//    @Override
//    public void dragStop(InputEvent event, float x, float y, int pointer, DragAndDrop.Payload payload, DragAndDrop.Target target) {
//        if (target != null) return;
//        eventBus.fire(new InventoryDropEvent(fromIdx));
//    }

    public Image getImage() {
        return image;
    }

    public int getFromIdx() {
        return fromIdx;
    }
}
