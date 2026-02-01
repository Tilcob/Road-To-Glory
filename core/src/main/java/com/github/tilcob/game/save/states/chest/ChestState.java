package com.github.tilcob.game.save.states.chest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.tilcob.game.item.ItemDefinitionRegistry;

import java.util.ArrayList;
import java.util.List;

public class ChestState {
    @JsonIgnore
    private final List<String> contents = new ArrayList<>();
    private List<String> contentsByName = new ArrayList<>();
    private boolean opened;

    public ChestState() {}

    public ChestState(Array<String> initialLoot) {
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
    public List<String> getContents() { return contents; }

    @JsonIgnore
    public void rebuildContentsFromName() {
        contents.clear();
        List<String> normalized = new ArrayList<>();
        for (String name : contentsByName) {
            String resolved = ItemDefinitionRegistry.resolveId(name);
            if (ItemDefinitionRegistry.hasDefinitions() && !ItemDefinitionRegistry.isKnownId(resolved)) {
                Gdx.app.error("ChestState", "Unknown item id: " + name);
                continue;
            }
            contents.add(resolved);
            normalized.add(resolved);
        }
        contentsByName = normalized;
        opened = false;
    }

    @JsonIgnore
    public Array<String> getContentsForGame() {
        Array<String> newArray = new Array<>();
        for (String item : contents) {
            newArray.add(item);
        }
        return newArray;
    }

    @JsonIgnore
    public void setContentsForGame(Array<String> contents) {
        clearContents();
        addItems(contents);
    }

    @JsonIgnore
    private void addItems(Array<String> contents) {
        for (String item : contents) {
            this.contents.add(item);
            contentsByName.add(item);
        }
    }

    @JsonIgnore
    public void open() { opened = true; }

    @JsonIgnore
    public void close() { opened = false; }

    @JsonIgnore
    public void clearContents() {
        contents.clear();
        contentsByName.clear();
    }

    @JsonIgnore
    public void setContents(List<String> newContents) {
        contents.clear();
        contentsByName.clear();
        for (String item : newContents) {
            contents.add(item);
            contentsByName.add(item);
        }
    }
}
