package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.Id;
import com.github.tilcob.game.component.Player;
import com.github.tilcob.game.component.StatComponent;
import com.github.tilcob.game.component.StatModifierComponent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.StatRecalcEvent;
import com.github.tilcob.game.stat.StatApplier;
import com.github.tilcob.game.stat.StatModifier;
import com.github.tilcob.game.stat.StatType;

public class StatRecalcSystem extends EntitySystem implements Disposable {
    private static final String TAG = "StatRecalcSystem";
    private static final String ITEM_SOURCE_PREFIX = "item:";
    private static final String BUFF_SOURCE_PREFIX = "buff:";
    private static final String LEVEL_UP_SOURCE_PREFIX = "levelup:";

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
        StatApplier.apply(entity, stats);

        if (Gdx.app != null && Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
            logStats(entity, stats, modifiers);
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

    private static void logStats(Entity entity, StatComponent stats, StatModifierComponent modifiers) {
        StringBuilder builder = new StringBuilder();
        builder.append("Recalc entity=");
        Id id = Id.MAPPER.get(entity);
        if (id != null) {
            builder.append(id.getId());
        } else {
            if (Player.MAPPER.has(entity)) builder.append("Player");
            else builder.append(entity);
        }
        builder.append('\n');
        builder.append("BaseStats: ").append(formatStats(stats, true)).append('\n');
        builder.append("FinalStats: ").append(formatStats(stats, false)).append('\n');
        builder.append("Modifiers:");
        if (modifiers == null || modifiers.getModifiers().size == 0) {
            builder.append(" (none)");
        } else {
            for (StatModifier modifier : modifiers.getModifiers()) {
                if (modifier == null) {
                    continue;
                }
                builder.append('\n')
                    .append("  - [")
                    .append(resolveSourceType(modifier.getSource()))
                    .append("] ")
                    .append(modifier.getStatType())
                    .append(" add=")
                    .append(modifier.getAdditive())
                    .append(" mult=")
                    .append(modifier.getMultiplier())
                    .append(" source=")
                    .append(modifier.getSource());
            }
        }
        Gdx.app.debug(TAG, builder.toString());
    }

    private static String formatStats(StatComponent stats, boolean base) {
        StringBuilder builder = new StringBuilder();
        for (StatType type : StatType.values()) {
            if (builder.length() > 0) builder.append(", ");
            float value = base ? stats.getBaseStat(type) : stats.getFinalStat(type);
            builder.append(type).append('=').append(value);
        }
        return builder.toString();
    }

    private static String resolveSourceType(String source) {
        if (source == null) {
            return "UNKNOWN";
        }
        if (source.startsWith(ITEM_SOURCE_PREFIX)) {
            return "ITEM";
        }
        if (source.startsWith(BUFF_SOURCE_PREFIX)) {
            return "BUFF";
        }
        if (source.startsWith(LEVEL_UP_SOURCE_PREFIX)) {
            return "LEVEL_UP";
        }
        return "UNKNOWN";
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(StatRecalcEvent.class, this::onStatRecalc);
    }
}
