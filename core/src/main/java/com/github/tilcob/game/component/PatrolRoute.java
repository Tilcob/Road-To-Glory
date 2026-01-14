package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class PatrolRoute implements Component {
    public static final ComponentMapper<PatrolRoute> MAPPER = ComponentMapper.getFor(PatrolRoute.class);

    private final Array<Vector2> points;
    private final boolean loop;
    private int index;
    private float waitTimeSeconds;
    private float waitTimer;

    public PatrolRoute(Array<Vector2> points, boolean loop, float waitTimeSeconds) {
        this.points = points;
        this.loop = loop;
        this.index = 0;
        this.waitTimeSeconds = waitTimeSeconds;
        this.waitTimer = 0f;
    }

    public Array<Vector2> getPoints() {
        return points;
    }

    public Vector2 getCurrentPoint() {
        if (points.isEmpty()) return null;
        return points.get(index);
    }

    public Vector2 advance() {
        if (points.isEmpty()) return null;
        index++;
        if (index >= points.size) {
            if (loop) {
                index = 0;
            } else {
                index = points.size - 1;
            }
        }
        return points.get(index);
    }

    public void setWaitTimer() {
        waitTimer = waitTimeSeconds;
    }

    public void updateWaitTimer(float deltaSeconds) {
        waitTimer = Math.max(0f, waitTimer - deltaSeconds);
    }

    public boolean isWaiting() {
        return waitTimer > 0f;
    }
}
