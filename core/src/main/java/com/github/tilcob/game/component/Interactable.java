package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.Gdx;

public class Interactable implements Component {
    public static final ComponentMapper<Interactable> mapper = ComponentMapper.getFor(Interactable.class);

    private int priority = 0;

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        if (priority < 0 || priority > 10) {
            Gdx.app.log("Interactable", "Invalid priority! Priority must be between 0 and 10");
        }
        this.priority = priority;
    }
}
