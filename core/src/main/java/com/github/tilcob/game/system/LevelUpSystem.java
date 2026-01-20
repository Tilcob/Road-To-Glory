package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.component.StatComponent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.LevelUpEvent;
import com.github.tilcob.game.event.StatRecalcEvent;
import com.github.tilcob.game.stat.StatType;

public class LevelUpSystem extends EntitySystem implements Disposable {
    private static final float ATTACK_PER_LEVEL = 1f;
    private static final float DAMAGE_PER_LEVEL = 1f;
    private static final float MAX_LIFE_PER_LEVEL = 5f;

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

        float levels = event.levelsGained();
        stats.setBaseStat(StatType.ATTACK, stats.getBaseStat(StatType.ATTACK) + (ATTACK_PER_LEVEL * levels));
        stats.setBaseStat(StatType.DAMAGE, stats.getBaseStat(StatType.DAMAGE) + (DAMAGE_PER_LEVEL * levels));
        stats.setBaseStat(StatType.MAX_LIFE, stats.getBaseStat(StatType.MAX_LIFE) + (MAX_LIFE_PER_LEVEL * levels));

        eventBus.fire(new StatRecalcEvent(entity));
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(LevelUpEvent.class, this::onLevelUp);
    }
}
