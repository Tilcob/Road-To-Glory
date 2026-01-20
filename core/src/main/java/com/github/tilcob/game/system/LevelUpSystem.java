package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.StatComponent;
import com.github.tilcob.game.component.StatModifierComponent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.LevelUpEvent;
import com.github.tilcob.game.event.StatRecalcEvent;
import com.github.tilcob.game.stat.StatModifier;
import com.github.tilcob.game.stat.StatType;

public class LevelUpSystem extends EntitySystem implements Disposable {
    private static final float ATTACK_PER_LEVEL = 1f;
    private static final float DAMAGE_PER_LEVEL = 1f;
    private static final float MAX_LIFE_PER_LEVEL = 5f;
    private static final String LEVEL_UP_SOURCE_PREFIX = "levelup:";

    private final GameEventBus eventBus;

    public LevelUpSystem(GameEventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.subscribe(LevelUpEvent.class, this::onLevelUp);
    }

    private void onLevelUp(LevelUpEvent event) {
        Entity entity = event.entity();
        if (entity == null || event.levelsGained() <= 0) {
            return;
        }

        StatComponent stats = StatComponent.MAPPER.get(entity);
        if (stats == null) {
            stats = new StatComponent();
            entity.add(stats);
        }

        StatModifierComponent modifiers = StatModifierComponent.MAPPER.get(entity);
        if (modifiers == null) {
            modifiers = new StatModifierComponent();
            entity.add(modifiers);
        }

        int totalLevels = event.newLevel() > 0 ? Math.max(0, event.newLevel() - 1) : event.levelsGained();
        modifiers.removeModifiersBySourcePrefix(LEVEL_UP_SOURCE_PREFIX);
        if (totalLevels > 0) {
            String source = LEVEL_UP_SOURCE_PREFIX + totalLevels;
            modifiers.addModifier(new StatModifier(StatType.ATTACK, ATTACK_PER_LEVEL * totalLevels, 0f, source));
            modifiers.addModifier(new StatModifier(StatType.DAMAGE, DAMAGE_PER_LEVEL * totalLevels, 0f, source));
            modifiers.addModifier(new StatModifier(StatType.MAX_LIFE, MAX_LIFE_PER_LEVEL * totalLevels, 0f, source));
        }

        if (Gdx.app != null && Gdx.app.getLogLevel() >= Application.LOG_DEBUG) {
            Gdx.app.debug(
                "LevelUpSystem",
                "Applied level-up modifiers for entity. levelsGained="
                    + event.levelsGained()
                    + ", newLevel="
                    + event.newLevel()
                    + ", totalLevels="
                    + totalLevels
            );
        }

        eventBus.fire(new StatRecalcEvent(entity));
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(LevelUpEvent.class, this::onLevelUp);
    }
}
