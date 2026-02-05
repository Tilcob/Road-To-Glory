package com.github.tilcob.game.event;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.OverheadIndicator;

public record PlayIndicatorEvent(Entity player, Entity target,
                                 OverheadIndicator.OverheadIndicatorType indicatorType,
                                 Float durationSeconds) {
    public PlayIndicatorEvent(Entity player, Entity target,
                              OverheadIndicator.OverheadIndicatorType indicatorType) {
        this(player, target, indicatorType, null);
    }
}
