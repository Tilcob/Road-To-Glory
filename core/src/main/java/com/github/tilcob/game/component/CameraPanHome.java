package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Vector2;

public class CameraPanHome implements Component {
    public static final ComponentMapper<CameraPanHome> MAPPER = ComponentMapper.getFor(CameraPanHome.class);

    private final Vector2 position = new Vector2();

    public CameraPanHome(float x, float y) {
        position.set(x, y);
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(float x, float y) {
        position.set(x, y);
    }
}
