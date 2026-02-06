package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class OverheadIndicator implements Component {
    public static final ComponentMapper<OverheadIndicator> MAPPER = ComponentMapper.getFor(OverheadIndicator.class);

    private OverheadIndicatorType indicatorId;
    private final Vector2 offset;
    private float baseScale;
    private Color color;
    private boolean visible;
    private Boolean allowPulse;
    private Boolean allowBob;

    public OverheadIndicator(OverheadIndicatorType indicatorId, Vector2 offset, float baseScale, Color color, boolean visible) {
        this.indicatorId = indicatorId;
        this.offset = offset;
        this.baseScale = baseScale;
        this.color = color;
        this.visible = visible;
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

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
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
}
