package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.MathUtils;

public class Life implements Component {
    public static final ComponentMapper<Life> MAPPER = ComponentMapper.getFor(Life.class);

    private final float baseMaxLife;
    private final float baseLifePerSec;
    private float maxLife;
    private float life;
    private float lifePerSec;

    public Life(float maxLife, float lifePerSec) {
        this.maxLife = maxLife;
        this.baseMaxLife = maxLife;
        this.baseLifePerSec = lifePerSec;
        this.life = maxLife;
        this.lifePerSec = lifePerSec;
    }

    public void addLife(float value) {
        life = MathUtils.clamp(life + value, 0f, maxLife);
    }

    public float getLifePerSec() {
        return lifePerSec;
    }

    public void setLifePerSec(float lifePerSec) {
        this.lifePerSec = lifePerSec;
    }

    public float getLife() {
        return life;
    }

    public void setLife(float life) {
        this.life = life;
    }

    public float getBaseMaxLife() {
        return baseMaxLife;
    }

    public float getBaseLifePerSec() {
        return baseLifePerSec;
    }

    public float getMaxLife() {
        return maxLife;
    }

    public void setMaxLife(float maxLife) {
        this.maxLife = maxLife;
    }
}
