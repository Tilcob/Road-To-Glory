package com.github.tilcob.game.ui.inventory.chest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

public class ChestItemSource extends DragAndDrop.Source {
    private final Image image;
    private final int fromIdx;

    public ChestItemSource(Image image, int fromIdx) {
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

    public Image getImage() {
        return image;
    }

    public int getFromIdx() {
        return fromIdx;
    }
}
