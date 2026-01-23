package com.github.tilcob.game.stat;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.*;

public final class StatApplier {
    private StatApplier() {
    }

    public static void apply(Entity entity, StatComponent stats) {
        if (entity == null || stats == null) {
            return;
        }
        applyAttack(entity, stats);
        applyLife(entity, stats);
        applyProtection(entity, stats);
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

    private static void applyProtection(Entity entity, StatComponent stats) {
        Protection protection = Protection.MAPPER.get(entity);
        if (protection == null) return;
        StatModifierComponent modifierComp = StatModifierComponent.MAPPER.get(entity);
        float damageMultiplier = 1f;
        if (modifierComp != null) {
            for (StatModifier modifier : modifierComp.getModifiers()) {
                if (modifier == null) continue;
                if (modifier.getStatType() != StatType.PROTECTION) continue;
                float percentage = Math.min(Math.max(0f, Math.min(1f, modifier.getAdditive() / 100f)), .95f);
                damageMultiplier *= (1f - percentage);
            }
        }
        stats.setFinalStat(StatType.PROTECTION, Math.min(95f, (1 - damageMultiplier) * 100f));
        protection.setProtection(Math.min(95f, (1 - damageMultiplier) * 100f));

    }
}
