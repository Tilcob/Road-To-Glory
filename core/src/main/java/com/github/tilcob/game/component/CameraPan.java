package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Vector2;

public class CameraPan implements Component {
    public static final ComponentMapper<CameraPan> MAPPER = ComponentMapper.getFor(CameraPan.class);

    private final Vector2 start = new Vector2();
    private final Vector2 target = new Vector2();
    private boolean startInitialized = false;

    private float durationSeconds;
    private float elapsedSeconds;

    public CameraPan(float targetX, float targetY, float durationSeconds) {
        this.target.set(targetX, targetY);
        this.durationSeconds = Math.max(0f, durationSeconds);
        this.elapsedSeconds = 0f;
    }

    public Vector2 getStart() { return start; }
    public Vector2 getTarget() { return target; }

    public boolean isStartInitialized() { return startInitialized; }

    public void initStart(float x, float y) {
        if (startInitialized) return;
        start.set(x, y);
        startInitialized = true;
    }

    public void update(float deltaSeconds) {
        elapsedSeconds += deltaSeconds;
    }

    public float alpha() {
        if (durationSeconds <= 0f) return 1f;
        return Math.min(1f, elapsedSeconds / durationSeconds);
    }

    public boolean isComplete() {
        return alpha() >= 1f;
    }
}
