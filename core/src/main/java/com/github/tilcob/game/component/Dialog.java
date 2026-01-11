package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

public class Dialog implements Component {
    public static final ComponentMapper<Dialog> MAPPER = ComponentMapper.getFor(Dialog.class);

    private State state = State.IDLE;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public enum State {
        IDLE,
        REQUEST,
        ACTIVE,
        FINISHED,
    }
}
