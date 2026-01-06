package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.item.ItemType;
import com.github.tilcob.game.registry.ChestRegistry;

public class Chest implements Component {
    public static final ComponentMapper<Chest> MAPPER = ComponentMapper.getFor(Chest.class);

    private final ChestRegistry.ChestState state;

    public Chest(ChestRegistry.ChestState state) {
        this.state = state;
    }

    public void open() {
        state.open();
    }

    public void close() {
        state.close();
    }

    public boolean isOpen() {
        return state.isOpened();
    }

    public Array<ItemType> getContents() {
        return state.getContents();
    }
}
