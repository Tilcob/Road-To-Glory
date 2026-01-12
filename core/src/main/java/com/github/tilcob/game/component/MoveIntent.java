package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.config.Constants;

public class MoveIntent implements Component {
    public static final ComponentMapper<MoveIntent> MAPPER = ComponentMapper.getFor(MoveIntent.class);

    private final Vector2 direction = new Vector2();
    private final Vector2 target = new Vector2();
    private boolean useTarget;
    private boolean active;
    private float arrivalDistance = Constants.DEFAULT_ARRIVAL_DISTANCE;

    public Vector2 getDirection() {
        return direction;
    }

    public Vector2 getTarget() {
        return target;
    }

    public boolean isUseTarget() {
        return useTarget;
    }

    public boolean isActive() {
        return active;
    }

    public float getArrivalDistance() {
        return arrivalDistance;
    }

    public void setDirection(Vector2 direction) {
        this.direction.set(direction);
        this.useTarget = false;
        this.active = true;
    }

    public void setDirection(float x, float y) {
        this.direction.set(x, y);
        this.useTarget = false;
        this.active = true;
    }

    public void setTarget(Vector2 target, float arrivalDistance) {
        this.target.set(target);
        this.arrivalDistance = arrivalDistance;
        this.useTarget = true;
        this.active = true;
    }

    public void clear() {
        direction.setZero();
        target.setZero();
        useTarget = false;
        active = false;
        arrivalDistance = Constants.DEFAULT_ARRIVAL_DISTANCE;
    }
}
