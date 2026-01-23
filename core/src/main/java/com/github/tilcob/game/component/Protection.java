package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.MathUtils;

public class Protection implements Component {
    public static final ComponentMapper<Protection> MAPPER = ComponentMapper.getFor(Protection.class);

    private float protection;

    public float getProtection() {
        return protection;
    }

    public void setProtection(float protection) {
        this.protection = MathUtils.clamp(protection, 0f, 95f);
    }
}
