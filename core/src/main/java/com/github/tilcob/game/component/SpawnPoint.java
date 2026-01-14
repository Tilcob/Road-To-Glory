package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Vector2;

public class SpawnPoint implements Component {
    public static final ComponentMapper<SpawnPoint> MAPPER = ComponentMapper.getFor(SpawnPoint.class);

    private final Vector2 position;

    public SpawnPoint(Vector2 position) {
        this.position = new Vector2(position);
    }

    public Vector2 getPosition() {
        return position;
    }
}
