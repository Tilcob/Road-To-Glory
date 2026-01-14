package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Vector2;

public class AggroMemory implements Component {
    public static final ComponentMapper<AggroMemory> MAPPER = ComponentMapper.getFor(AggroMemory.class);

    private final Vector2 lastKnownPosition;
    private float timeSinceSeen;
    private boolean hasAggro;

    public AggroMemory() {
        this.lastKnownPosition = new Vector2();
        this.timeSinceSeen = 0f;
        this.hasAggro = false;
    }

    public void markSeen(Vector2 position) {
        if (position != null) {
            lastKnownPosition.set(position);
        }
        timeSinceSeen = 0f;
        hasAggro = true;
    }

    public void decay(float deltaSeconds) {
        if (!hasAggro) return;
        timeSinceSeen += deltaSeconds;
    }

    public boolean isExpired(float forgetTimeSeconds) {
        return hasAggro && timeSinceSeen >= forgetTimeSeconds;
    }

    public void clear() {
        timeSinceSeen = 0f;
        hasAggro = false;
    }

    public Vector2 getLastKnownPosition() {
        return lastKnownPosition;
    }

    public boolean hasAggro() {
        return hasAggro;
    }
}
