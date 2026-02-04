package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class OverheadIndicatorAnimation implements Component {
    public static final ComponentMapper<OverheadIndicatorAnimation> MAPPER = ComponentMapper.getFor(OverheadIndicatorAnimation.class);

    private float time;
    private float bobPhase;
    private float pulsePhase;
    private float currentOffsetY;
    private float currentScale;

    public OverheadIndicatorAnimation(float time, float bobPhase, float pulsePhase, float currentOffsetY, float currentScale) {
        this.time = time;
        this.bobPhase = bobPhase;
        this.pulsePhase = pulsePhase;
        this.currentOffsetY = currentOffsetY;
        this.currentScale = currentScale;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public float getBobPhase() {
        return bobPhase;
    }

    public void setBobPhase(float bobPhase) {
        this.bobPhase = bobPhase;
    }

    public float getPulsePhase() {
        return pulsePhase;
    }

    public void setPulsePhase(float pulsePhase) {
        this.pulsePhase = pulsePhase;
    }

    public float getCurrentOffsetY() {
        return currentOffsetY;
    }

    public void setCurrentOffsetY(float currentOffsetY) {
        this.currentOffsetY = currentOffsetY;
    }

    public float getCurrentScale() {
        return currentScale;
    }

    public void setCurrentScale(float currentScale) {
        this.currentScale = currentScale;
    }
}
