package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.github.tilcob.game.component.OverheadIndicator;
import com.github.tilcob.game.component.OverheadIndicatorAnimation;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.indicator.IndicatorVisualDef;
import com.github.tilcob.game.indicator.OverheadIndicatorRegistry;

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

        IndicatorVisualDef visualDef = OverheadIndicatorRegistry.getVisualDef(indicator.getIndicatorId());
        float bobSpeed = visualDef == null ? BOB_SPEED : visualDef.bobSpeed();
        float pulseSpeed = visualDef == null ? PULSE_SPEED : visualDef.pulseSpeed();
        float bobAmplitude = visualDef == null ? BOB_AMPLITUDE : visualDef.bobAmplitude();
        float pulseAmplitude = visualDef == null ? PULSE_AMPLITUDE : visualDef.pulseAmplitude();

        float bobPhase = animation.getBobPhase() + deltaTime * bobSpeed;
        float pulsePhase = animation.getPulsePhase() + deltaTime * pulseSpeed;
        animation.setBobPhase(bobPhase);
        animation.setPulsePhase(pulsePhase);

        boolean allowBob = indicator.getAllowBob() == null || indicator.getAllowBob();
        boolean allowPulse = indicator.getAllowPulse() == null || indicator.getAllowPulse();

        float currentOffsetY = allowBob ? MathUtils.sin(bobPhase) * bobAmplitude : 0f;
        float currentScale = allowPulse ? 1f + MathUtils.sin(pulsePhase) * pulseAmplitude : 1f;
        animation.setCurrentOffsetY(currentOffsetY);
        animation.setCurrentScale(currentScale);
    }
}
