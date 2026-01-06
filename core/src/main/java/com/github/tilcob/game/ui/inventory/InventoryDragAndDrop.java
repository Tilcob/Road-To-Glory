package com.github.tilcob.game.ui.inventory;

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;

public class InventoryDragAndDrop {
    private final DragAndDrop dragAndDrop;

    public InventoryDragAndDrop() {
        this.dragAndDrop = new DragAndDrop();
        dragAndDrop.setDragActorPosition(0, 0);
    }

    public void addSource(DragAndDrop.Source source) {
        dragAndDrop.addSource(source);
    }

    public void addTarget(DragAndDrop.Target target) {
        dragAndDrop.addTarget(target);
    }

    public DragAndDrop getDragAndDrop() {
        return dragAndDrop;
    }
}
