package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.save.states.chest.ChestState;

import java.util.List;

public class Chest implements Component {
    public static final ComponentMapper<Chest> MAPPER = ComponentMapper.getFor(Chest.class);

    private final ChestState state;

    public Chest(ChestState state) {
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

    public Array<String> getContents() {
        return state.getContentsForGame();
    }

    public void clear() {
        state.getContents().clear();
        state.clearContents();
    }

    public void setContents(List<String> items) {
        state.setContents(items);
    }

    public void setContents(Array<String> items) {
        state.setContentsForGame(items);
    }
}
