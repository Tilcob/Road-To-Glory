package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Vector2;

public class Move implements Component {
    public static final ComponentMapper<Move> MAPPER = ComponentMapper.getFor(Move.class);

    private float maxSpeed;
    private final Vector2 direction;
    private boolean isRooted;

    public Move(float maxSpeed) {
        this.direction = new Vector2();
        this.maxSpeed = maxSpeed;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public Vector2 getDirection() {
        return direction;
    }

    public boolean isRooted() {
        return isRooted;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public void setRooted(boolean rooted) {
        isRooted = rooted;
    }
}
