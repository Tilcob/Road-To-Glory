package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.tilcob.game.component.StatModifierComponent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.StatRecalcEvent;
import com.github.tilcob.game.stat.StatModifier;
import com.github.tilcob.game.stat.StatModifierManager;

public class StatModifierDurationSystem extends IteratingSystem {
    private final StatModifierManager statModifierManager;

    public StatModifierDurationSystem(GameEventBus eventBus) {
        super(Family.all(StatModifierComponent.class).get());
        this.statModifierManager = new StatModifierManager(eventBus);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        statModifierManager.removeExpired(entity, TimeUtils.millis());
    }
}
