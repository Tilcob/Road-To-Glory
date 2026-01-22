package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.MathUtils;

public class ScreenFade implements Component {
    public static final ComponentMapper<ScreenFade> MAPPER = ComponentMapper.getFor(ScreenFade.class);

    private float currentAlpha;
    private float startAlpha;
    private float targetAlpha;
    private float durationSeconds;
    private float elapsedSeconds;
    private boolean active;

    public ScreenFade(float initialAlpha) {
        this.currentAlpha = MathUtils.clamp(initialAlpha, 0f, 1f);
        this.startAlpha = this.currentAlpha;
        this.targetAlpha = this.currentAlpha;
        this.durationSeconds = 0f;
        this.elapsedSeconds = 0f;
        this.active = false;
    }

    public float getCurrentAlpha() {
        return currentAlpha;
    }

    public float getTargetAlpha() {
        return targetAlpha;
    }

    public boolean isActive() {
        return active;
    }

    public void start(float targetAlpha, float durationSeconds) {
        this.startAlpha = currentAlpha;
        this.targetAlpha = MathUtils.clamp(targetAlpha, 0f, 1f);
        this.durationSeconds = Math.max(0f, durationSeconds);
        this.elapsedSeconds = 0f;
        if (this.durationSeconds == 0f) {
            this.currentAlpha = this.targetAlpha;
            this.active = false;
            return;
        }
        this.active = true;
    }

    public void update(float deltaSeconds) {
        if (!active) {
            return;
        }
        elapsedSeconds += deltaSeconds;
        float progress = durationSeconds <= 0f ? 1f : MathUtils.clamp(elapsedSeconds / durationSeconds, 0f, 1f);
        currentAlpha = MathUtils.lerp(startAlpha, targetAlpha, progress);
        if (progress >= 1f) {
            active = false;
        }
    }
}
