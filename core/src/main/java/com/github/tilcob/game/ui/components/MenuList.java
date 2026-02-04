package com.github.tilcob.game.ui.components;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;


public class MenuList {
    private final Table table;

    public MenuList() {
        this.table = new Table();
    }

    public Table getTable() {
        return table;
    }

    public Array<Group> getItems() {
        Array<Group> items = new Array<>();
        for (Actor actor : table.getChildren()) {
            if (actor instanceof Group group) items.add(group);
        }
        return items;
    }

    public void addItem(TextButton button) {
        addItem((Actor) button);
    }

    public void addItem(Actor item) {
        table.add(item).row();
    }
}
