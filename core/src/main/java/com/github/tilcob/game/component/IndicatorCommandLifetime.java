package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class IndicatorCommandLifetime implements Component {
    public static final ComponentMapper<IndicatorCommandLifetime> MAPPER =
        ComponentMapper.getFor(IndicatorCommandLifetime.class);

    private float remainingSeconds;
    private OverheadIndicator.OverheadIndicatorType fallbackIndicatorType;
    private boolean fallbackVisible;

    public IndicatorCommandLifetime(
        float remainingSeconds,
        OverheadIndicator.OverheadIndicatorType fallbackIndicatorType,
        boolean fallbackVisible
    ) {
        this.remainingSeconds = Math.max(0f, remainingSeconds);
        this.fallbackIndicatorType = fallbackIndicatorType;
        this.fallbackVisible = fallbackVisible;
    }

    public float getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(float remainingSeconds) {
        this.remainingSeconds = Math.max(0f, remainingSeconds);
    }

    public OverheadIndicator.OverheadIndicatorType getFallbackIndicatorType() {
        return fallbackIndicatorType;
    }

    public boolean isFallbackVisible() {
        return fallbackVisible;
    }
}
