package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.tilcob.game.component.IndicatorCommandLifetime;
import com.github.tilcob.game.component.OverheadIndicator;

public class IndicatorCommandLifetimeSystem extends IteratingSystem {
    public IndicatorCommandLifetimeSystem() {
        super(Family.all(OverheadIndicator.class, IndicatorCommandLifetime.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        IndicatorCommandLifetime lifetime = IndicatorCommandLifetime.MAPPER.get(entity);
        OverheadIndicator indicator = OverheadIndicator.MAPPER.get(entity);

        float remainingSeconds = lifetime.getRemainingSeconds() - deltaTime;
        lifetime.setRemainingSeconds(remainingSeconds);

        if (lifetime.getRemainingSeconds() > 0f) {
            return;
        }

        indicator.setCurrentType(lifetime.getFallbackIndicatorType());
        if (lifetime.isFallbackVisible()) {
            indicator.setDesiredType(lifetime.getFallbackIndicatorType());
        } else {
            indicator.setDesiredType(null);
        }
        entity.remove(IndicatorCommandLifetime.class);
    }
}
