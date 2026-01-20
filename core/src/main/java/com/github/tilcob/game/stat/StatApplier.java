package com.github.tilcob.game.stat;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.Attack;
import com.github.tilcob.game.component.Life;
import com.github.tilcob.game.component.StatComponent;

public final class StatApplier {
    private StatApplier() {
    }

    public static void apply(Entity entity, StatComponent stats) {
        if (entity == null || stats == null) {
            return;
        }
        applyAttack(entity, stats);
        applyLife(entity, stats);
    }

    private static void applyAttack(Entity entity, StatComponent stats) {
        Attack attack = Attack.MAPPER.get(entity);
        if (attack == null) {
            return;
        }
        float attackBonus = stats.getFinalStat(StatType.ATTACK) + stats.getFinalStat(StatType.DAMAGE);
        attack.setDamage(Math.max(0f, attack.getBaseDamage() + attackBonus));
    }

    private static void applyLife(Entity entity, StatComponent stats) {
        Life life = Life.MAPPER.get(entity);
        if (life == null) {
            return;
        }
        float maxLife = life.getBaseMaxLife() + stats.getFinalStat(StatType.MAX_LIFE);
        life.setMaxLife(Math.max(0f, maxLife));

        float regen = life.getBaseLifePerSec() + stats.getFinalStat(StatType.LIFE_REGENERATION);
        life.setLifePerSec(regen);
    }
}
