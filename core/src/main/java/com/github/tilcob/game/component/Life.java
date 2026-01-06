package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.MathUtils;

public class Life implements Component {
    public static final ComponentMapper<Life> MAPPER = ComponentMapper.getFor(Life.class);

    private float maxLife;
    private float life;
    private float lifePerSec;

    public Life(float maxLife, float lifePerSec) {
        this.maxLife = maxLife;
        this.life = maxLife;
        this.lifePerSec = lifePerSec;
    }

    public void addLife(float value) {
        life = MathUtils.clamp(life + value, 0f, maxLife);
    }

    public float getLifePerSec() {
        return lifePerSec;
    }

    public float getLife() {
        return life;
    }

    public void setLife(float life) {
        this.life = life;
    }

    public float getMaxLife() {
        return maxLife;
    }
}
