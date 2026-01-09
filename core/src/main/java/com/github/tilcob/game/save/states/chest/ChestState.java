package com.github.tilcob.game.save.states.chest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.tilcob.game.item.ItemType;

import java.util.ArrayList;
import java.util.List;

public class ChestState {
    @JsonIgnore
    private final List<ItemType> contents = new ArrayList<>();
    private List<String> contentsByName = new ArrayList<>();
    private boolean opened;

    public ChestState() {}

    public ChestState(Array<ItemType> initialLoot) {
        addItems(initialLoot);
    }

    public boolean isOpened() { return opened; }

    public void setOpened(boolean opened) { this.opened = opened; }

    public List<String> getContentsByName() {
        return contentsByName;
    }

    public void setContentsByName(List<String> contentsByName) {
        this.contentsByName = contentsByName;
    }

    @JsonIgnore
    public List<ItemType> getContents() { return contents; }

    @JsonIgnore
    public void rebuildContentsFromName() {
        contents.clear();
        for (String name : contentsByName) {
            try {
                contents.add(ItemType.valueOf(name));
            } catch (IllegalArgumentException e) {
                Gdx.app.error("ChestState", e.getMessage());
            }
        }
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
