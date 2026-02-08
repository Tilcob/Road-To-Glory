package com.github.tilcob.game.stat;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.StatModifierComponent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.StatRecalcEvent;

public class StatModifierManager {
    private static final String BUFF_SOURCE_PREFIX = "buff:";

    private final GameEventBus eventBus;

    public StatModifierManager(GameEventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void addModifier(Entity entity, StatModifier modifier) {
        if (entity == null || modifier == null) return;
        StatModifierComponent modifiers = StatModifierComponent.MAPPER.get(entity);
        if (modifiers == null) return;

        modifiers.addModifier(modifier);
        eventBus.fire(new StatRecalcEvent(entity));
    }

    public void removeModifier(Entity entity, StatModifier modifier) {
        if (entity == null || modifier == null) return;
        StatModifierComponent modifiers = StatModifierComponent.MAPPER.get(entity);
        if (modifiers == null) return;

        int before = modifiers.getModifiers().size;
        modifiers.removeModifier(modifier);
        if (modifiers.getModifiers().size != before) eventBus.fire(new StatRecalcEvent(entity));
    }

    public void removeExpired(Entity entity, long now) {
        if (entity == null) return;
        StatModifierComponent modifiers = StatModifierComponent.MAPPER.get(entity);
        if (modifiers == null) return;

        boolean removed = false;
        for (int i = modifiers.getModifiers().size - 1; i >= 0; i--) {
            StatModifier modifier = modifiers.getModifiers().get(i);
            if (modifier == null) {
                modifiers.getModifiers().removeIndex(i);
                removed = true;
                continue;
            }
            if (!isBuffSource(modifier.getSource())) continue;

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

        if (removed) eventBus.fire(new StatRecalcEvent(entity));
    }

    private static boolean isBuffSource(String source) {
        return source != null && source.startsWith(BUFF_SOURCE_PREFIX);
    }
}
