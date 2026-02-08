package com.github.tilcob.game.flow.commands;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.component.IndicatorCommandLifetime;
import com.github.tilcob.game.component.OverheadIndicator;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.PlayIndicatorEvent;
import com.github.tilcob.game.indicator.IndicatorVisualDef;
import com.github.tilcob.game.indicator.OverheadIndicatorRegistry;

final class IndicatorCommandUtil {
    private IndicatorCommandUtil() {
    }

    static void playIndicator(PlayIndicatorEvent event) {
        if (event.player() == null || event.target() == null || event.indicatorType() == null) return;

        OverheadIndicator indicator = OverheadIndicator.MAPPER.get(event.target());
        boolean hadIndicator = indicator != null;
        OverheadIndicator.OverheadIndicatorType fallbackType =
            hadIndicator ? indicator.getCurrentType() : event.indicatorType();
        boolean fallbackVisible = hadIndicator && indicator.isVisible();

        if (indicator == null) {
            Transform transform = Transform.MAPPER.get(event.target());
            float offsetY = Constants.DEFAULT_INDICATOR_OFFSET_Y;
            if (transform != null) {
                offsetY = transform.getSize().y + Constants.DEFAULT_INDICATOR_OFFSET_Y - 8f * Constants.UNIT_SCALE;
            }
            IndicatorVisualDef visualDef = OverheadIndicatorRegistry.getVisualDef(event.indicatorType());
            float baseScale = visualDef == null ? 0.6f : visualDef.defaultScale();

            indicator = new OverheadIndicator(
                event.indicatorType(),
                new Vector2(0f, offsetY),
                baseScale,
                Color.WHITE.cpy(),
                true
            );
            event.target().add(indicator);
        }

        indicator.setDesiredType(event.indicatorType());
        IndicatorVisualDef visualDef = OverheadIndicatorRegistry.getVisualDef(event.indicatorType());
        if (visualDef != null) {
            indicator.setBaseScale(visualDef.defaultScale());
        }

        if (event.durationSeconds() != null && event.durationSeconds() > 0f) {
            event.target().remove(IndicatorCommandLifetime.class);
            event.target().add(new IndicatorCommandLifetime(event.durationSeconds(), fallbackType, fallbackVisible));
        }

        indicator.setTime(0f);
        indicator.setBobPhase(0f);
        indicator.setPulsePhase(0f);
        indicator.setCurrentOffsetY(0f);
        indicator.setScale(1f);
    }
}
