package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class ExpMultiplier implements Component {
    public static final ComponentMapper<ExpMultiplier> MAPPER = ComponentMapper.getFor(ExpMultiplier.class);

    private final float expMultiplier;

    public ExpMultiplier(float expMultiplier) {
        this.expMultiplier = expMultiplier;
    }

    public float getExpMultiplier() {
        return expMultiplier;
    }
}
