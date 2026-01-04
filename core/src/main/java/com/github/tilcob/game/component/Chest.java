package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.item.ItemType;

public class Chest implements Component {
    public static final ComponentMapper<Chest> MAPPER = ComponentMapper.getFor(Chest.class);

    private final Array<ItemType> contents;
    private boolean isOpen = false;

    public Chest(Array<ItemType> contents) {
        this.contents = contents;
    }

    public void open() {
        isOpen = true;
    }

    public void close() {
        isOpen = false;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public Array<ItemType> getContents() {
        return contents;
    }
}
