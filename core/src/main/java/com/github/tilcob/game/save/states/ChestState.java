package com.github.tilcob.game.save.states;

import com.badlogic.gdx.utils.Array;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.tilcob.game.item.ItemType;
import java.util.ArrayList;
import java.util.List;

public class ChestState {
    private List<ItemType> contents = new ArrayList<>();
    private boolean opened;

    public ChestState() {}

    public ChestState(Array<ItemType> initialLoot) {
        addItems(initialLoot);
    }

    public boolean isOpened() { return opened; }

    public void setOpened(boolean opened) { this.opened = opened; }

    public List<ItemType> getContents() { return contents; }

    public void setContents(List<ItemType> contents) {
        this.contents = contents;
    }

    @JsonIgnore
    public Array<ItemType> getContentsForGame() {
        Array<ItemType> newArray = new Array<>();
        for (ItemType item : contents) {
            newArray.add(item);
        }
        return newArray;
    }

    @JsonIgnore
    private void addItems(Array<ItemType> contents) {
        for (ItemType item : contents) {
            this.contents.add(item);
        }
    }

    @JsonIgnore
    public void open() { opened = true; }

    @JsonIgnore
    public void close() { opened = false; }
}
