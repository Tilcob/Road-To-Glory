package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.StatComponent;
import com.github.tilcob.game.component.StatModifierComponent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.StatRecalcEvent;
import com.github.tilcob.game.stat.StatModifier;
import com.github.tilcob.game.stat.StatType;

public class StatRecalcSystem extends EntitySystem implements Disposable {
    private final GameEventBus eventBus;

    public StatRecalcSystem(GameEventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.subscribe(StatRecalcEvent.class, this::onStatRecalc);
    }

    private void onStatRecalc(StatRecalcEvent event) {
        Entity entity = event.entity();
        if (entity == null) {
            return;
        }

        StatComponent stats = StatComponent.MAPPER.get(entity);
        if (stats == null) {
            return;
        }

        StatModifierComponent modifiers = StatModifierComponent.MAPPER.get(entity);
        for (StatType type : StatType.values()) {
            float base = stats.getBaseStat(type);
            float total = getTotal(type, modifiers, base);
            stats.setFinalStat(type, total);
        }
    }

    private static float getTotal(StatType type, StatModifierComponent modifiers, float base) {
        float additive = 0f;
        float multiplier = 0f;
        if (modifiers != null) {
            for (StatModifier modifier : modifiers.getModifiers()) {
                if (modifier == null || modifier.getStatType() != type) {
                    continue;
                }
                additive += modifier.getAdditive();
                multiplier += modifier.getMultiplier();
            }
        }
        return (base + additive) * (1f + multiplier);
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(StatRecalcEvent.class, this::onStatRecalc);
    }
}
