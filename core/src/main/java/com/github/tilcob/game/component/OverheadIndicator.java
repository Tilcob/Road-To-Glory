package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class OverheadIndicator implements Component {
    public static final ComponentMapper<OverheadIndicator> MAPPER = ComponentMapper.getFor(OverheadIndicator.class);

    private OverheadIndicatorType currentType;
    private OverheadIndicatorType desiredType;
    private State state;
    private float timer;
    private boolean visible;
    private float alpha;
    private float scale;
    private OverheadIndicatorType indicatorId;
    private final Vector2 offset;
    private float baseScale;
    private Color color;
    private float time;
    private float bobPhase;
    private float pulsePhase;
    private float currentOffsetY;
    private Boolean allowPulse;
    private Boolean allowBob;

    public OverheadIndicator(OverheadIndicatorType currentType,
                             Vector2 offset, float baseScale, Color color, boolean visible) {
        this.currentType = currentType;
        this.state = State.HIDDEN;
        this.timer = 0f;
        this.alpha = 1f;
        this.scale = 1f;
        this.offset = offset;
        this.baseScale = baseScale;
        this.color = color;
        this.visible = visible;
        this.time = 0f;
        this.bobPhase = 0f;
        this.pulsePhase = 0f;
        this.currentOffsetY = 0f;
    }

    public OverheadIndicatorType getCurrentType() {
        return currentType;
    }

    public void setCurrentType(OverheadIndicatorType currentType) {
        this.currentType = currentType;
    }

    public OverheadIndicatorType getDesiredType() {
        return desiredType;
    }

    public void setDesiredType(OverheadIndicatorType desiredType) {
        this.desiredType = desiredType;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public float getTimer() {
        return timer;
    }

    public void setTimer(float timer) {
        this.timer = timer;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public OverheadIndicatorType getIndicatorId() {
        return indicatorId;
    }

    public void setIndicatorId(OverheadIndicatorType indicatorId) {
        this.indicatorId = indicatorId;
    }

    public Vector2 getOffset() {
        return offset;
    }

    public float getBaseScale() {
        return baseScale;
    }

    public void setBaseScale(float baseScale) {
        this.baseScale = baseScale;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
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

    public Boolean getAllowPulse() {
        return allowPulse;
    }

    public void setAllowPulse(Boolean allowPulse) {
        this.allowPulse = allowPulse;
    }

    public Boolean getAllowBob() {
        return allowBob;
    }

    public void setAllowBob(Boolean allowBob) {
        this.allowBob = allowBob;
    }

    public enum OverheadIndicatorType {
        QUEST_AVAILABLE,
        QUEST_TURNING,
        DANGER,
        ANGRY,
        HAPPY,
        SAD,
        INFO,
        MERCHANT,
        TALK_AVAILABLE,
        TALK_IN_RANGE,
        TALK_BUSY,
        TALK_CHOICE,
        INTERACT_HINT,
        TALKING,
    }

    public enum State {
        HIDDEN,
        FADE_IN,
        IDLE,
        ATTENTION,
        FADE_OUT
    }
}
