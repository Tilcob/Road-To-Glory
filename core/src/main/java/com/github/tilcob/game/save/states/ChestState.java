package com.github.tilcob.game.save.states;

import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.item.ItemType;

public class ChestState {
    private final Array<ItemType> contents;
    private boolean opened;

    public ChestState(Array<ItemType> initialLoot) {
        this.contents = new Array<>(initialLoot);
    }

    public boolean isOpened() { return opened; }
    public void open() { opened = true; }
    public void close() { opened = false; }
    public Array<ItemType> getContents() { return contents; }
}
