package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.Pool;
import com.github.tilcob.game.config.Constants;

public class WanderTimer implements Component, Pool.Poolable {
    public static final ComponentMapper<WanderTimer> MAPPER = ComponentMapper.getFor(WanderTimer.class);

    private float timer = 0f;
    private float interval = Constants.WANDER_TIMER_INTERVAL;

    public void resetTimer() {
        this.timer = this.interval;
    }

    public void decreaseTimer(float delta) {
        this.timer -= delta;
    }

    public float getTimer() {
        return timer;
    }

    @Override
    public void reset() {
        timer = 0f;
        interval = Constants.WANDER_TIMER_INTERVAL;
    }
}
