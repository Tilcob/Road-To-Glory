package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Vector2;

public class CameraPan implements Component {
    public static final ComponentMapper<CameraPan> MAPPER = ComponentMapper.getFor(CameraPan.class);

    private final Vector2 target;
    private float durationSeconds;
    private float elapsedSeconds;

    public CameraPan(float x, float y, float durationSeconds) {
        this.target = new Vector2(x, y);
        this.durationSeconds = Math.max(0f, durationSeconds);
        this.elapsedSeconds = 0f;
    }

    public Vector2 getTarget() {
        return target;
    }

    public void update(float deltaSeconds) {
        elapsedSeconds += deltaSeconds;
    }

    public boolean isComplete() {
        return durationSeconds > 0f && elapsedSeconds >= durationSeconds;
    }

    public float getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(float durationSeconds) {
        this.durationSeconds = Math.max(0f, durationSeconds);
    }

    public float getElapsedSeconds() {
        return elapsedSeconds;
    }

    public void setElapsedSeconds(float elapsedSeconds) {
        this.elapsedSeconds = Math.max(0f, elapsedSeconds);
    }
}
