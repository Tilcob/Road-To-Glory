package com.github.tilcob.game.ui.inventory;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.github.tilcob.game.item.ItemCategory;

public class EquipmentItemSource extends DragAndDrop.Source {
    private final Image image;
    private final int fromIdx;
    private final ItemCategory category;

    public EquipmentItemSource(Image image, int fromIdx, ItemCategory category) {
        super(image);
        this.image = image;
        this.fromIdx = fromIdx;
        this.category = category;
    }

    @Override
    public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
        DragAndDrop.Payload payload = new DragAndDrop.Payload();
        payload.setObject(this);
        payload.setDragActor(new Image(image.getDrawable()));
        return payload;
    }

    public int getFromIdx() {
        return fromIdx;
    }

    public ItemCategory getCategory() {
        return category;
    }
}
