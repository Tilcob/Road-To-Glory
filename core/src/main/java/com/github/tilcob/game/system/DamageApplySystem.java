package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.event.EntityDamagedEvent;
import com.github.tilcob.game.event.GameEventBus;

public class DamageApplySystem extends IteratingSystem {
    private final GameEventBus eventBus;

    public DamageApplySystem(GameEventBus eventBus) {
        super(Family.all(Damaged.class).get());
        this.eventBus = eventBus;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Damaged damaged = Damaged.MAPPER.get(entity);
        entity.remove(Damaged.class);

        Life life = Life.MAPPER.get(entity);
        if (life == null) return;

        Protection protection = Protection.MAPPER.get(entity);
        float dealtDamage = damaged.getDamage();
        if (protection != null) {
            dealtDamage *= (1f - Math.max(0f, Math.min(.95f, protection.getProtection() / 100f)));
        }

        dealtDamage = Math.min(dealtDamage, life.getLife());
        life.addLife(-dealtDamage);

        Transform transform = Transform.MAPPER.get(entity);
        if (transform != null && dealtDamage > 0f && Player.MAPPER.get(entity) != null) {
            eventBus.fire(new EntityDamagedEvent(entity, dealtDamage));
        }
    }
}
