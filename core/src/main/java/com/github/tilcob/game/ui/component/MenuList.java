package com.github.tilcob.game.ui.component;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;


public class MenuList {
    private final Table rootTable;
    private final Table listTable;

    public MenuList() {
        rootTable = new Table();
        listTable = new Table();
        rootTable.add(listTable).row();
    }

    public Table getRootTable() {
        return rootTable;
    }

    public Table getListTable() {
        return listTable;
    }

    public void addItem(TextButton button) {
        addItem((Actor) button);
    }

    public void addItem(Actor item) {
        listTable.add(item).row();
    }
}
