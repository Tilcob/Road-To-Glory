package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Vector2;

public class Transform implements Component, Comparable<Transform> {
    public static final ComponentMapper<Transform> MAPPER = ComponentMapper.getFor(Transform.class);

    private final Vector2 position;
    private final int z;
    private final Vector2 size;
    private final Vector2 scaling;
    private float rotationDegrees;

    public Transform(Vector2 position, int z, Vector2 size, Vector2 scaling, float rotationDegrees) {
        this.position = position;
        this.z = z;
        this.size = size;
        this.scaling = scaling;
        this.rotationDegrees = rotationDegrees;
    }

    @Override
    public int compareTo(Transform o) {
        if (this.z != o.z) {
            return Float.compare(this.z, o.z);
        }
        if (this.position.y != o.position.y) {
            return Float.compare(o.position.y, this.position.y);
        }
        return Float.compare(this.position.x, o.position.x);

    }

    public Vector2 getPosition() {
        return position;
    }

    public int getZ() {
        return z;
    }

    public Vector2 getSize() {
        return size;
    }

    public Vector2 getScaling() {
        return scaling;
    }

    public float getRotationDegrees() {
        return rotationDegrees;
    }

    public void setRotationDegrees(float rotationDegrees) {
        this.rotationDegrees = rotationDegrees;
    }
}
