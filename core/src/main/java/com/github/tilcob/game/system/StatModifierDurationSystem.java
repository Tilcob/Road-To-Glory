package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.tilcob.game.component.StatModifierComponent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.StatRecalcEvent;
import com.github.tilcob.game.stat.StatModifier;

public class StatModifierDurationSystem extends IteratingSystem {
    private final GameEventBus eventBus;

    public StatModifierDurationSystem(GameEventBus eventBus) {
        super(Family.all(StatModifierComponent.class).get());
        this.eventBus = eventBus;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        StatModifierComponent modifiers = StatModifierComponent.MAPPER.get(entity);
        if (modifiers == null) return;

        boolean removed = false;
        long now = TimeUtils.millis();
        for (int i = modifiers.getModifiers().size - 1; i >= 0; i--) {
            StatModifier modifier = modifiers.getModifiers().get(i);
            if (modifier == null) {
                modifiers.getModifiers().removeIndex(i);
                removed = true;
                continue;
            }
            if (modifier.getDurationSeconds() != null && modifier.getExpireTimeEpochMs() == null) {
                long durationMs = Math.max(0L, Math.round(modifier.getDurationSeconds() * 1000f));
                modifier.setExpireTimeEpochMs(now + durationMs);
            }
            Long expireTime = modifier.getExpireTimeEpochMs();
            if (expireTime != null && expireTime <= now) {
                modifiers.getModifiers().removeIndex(i);
                removed = true;
            }
        }

        if (removed) {
            eventBus.fire(new StatRecalcEvent(entity));
        }
    }
}
