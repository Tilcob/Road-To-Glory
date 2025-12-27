package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class Transform implements Component {
    public static final ComponentMapper<Transform> mapper = ComponentMapper.getFor(Transform.class);

    public Transform() {

    }
}
