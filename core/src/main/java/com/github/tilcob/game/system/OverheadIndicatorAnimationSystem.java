package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.github.tilcob.game.component.OverheadIndicator;
import com.github.tilcob.game.component.OverheadIndicatorAnimation;
import com.github.tilcob.game.component.Transform;

public class OverheadIndicatorAnimationSystem extends IteratingSystem {
    private static final float BOB_AMPLITUDE = 0.15f;
    private static final float BOB_SPEED = 2.5f;
    private static final float PULSE_AMPLITUDE = 0.08f;
    private static final float PULSE_SPEED = 3.0f;

    public OverheadIndicatorAnimationSystem() {
        super(Family.all(OverheadIndicator.class, OverheadIndicatorAnimation.class, Transform.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        OverheadIndicator indicator = OverheadIndicator.MAPPER.get(entity);
        OverheadIndicatorAnimation animation = OverheadIndicatorAnimation.MAPPER.get(entity);

        animation.setTime(animation.getTime() + deltaTime);

        float bobPhase = animation.getBobPhase() + deltaTime * BOB_SPEED;
        float pulsePhase = animation.getPulsePhase() + deltaTime * PULSE_SPEED;
        animation.setBobPhase(bobPhase);
        animation.setPulsePhase(pulsePhase);

        boolean allowBob = indicator.getAllowBob() == null || indicator.getAllowBob();
        boolean allowPulse = indicator.getAllowPulse() == null || indicator.getAllowPulse();

        float currentOffsetY = allowBob ? MathUtils.sin(bobPhase) * BOB_AMPLITUDE : 0f;
        float currentScale = allowPulse ? 1f + MathUtils.sin(pulsePhase) * PULSE_AMPLITUDE : 1f;
        animation.setCurrentOffsetY(currentOffsetY);
        animation.setCurrentScale(currentScale);
    }
}
