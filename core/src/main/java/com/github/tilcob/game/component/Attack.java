package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.MathUtils;
import com.github.tilcob.game.assets.SoundAsset;

public class Attack implements Component {
    public static final ComponentMapper<Attack> MAPPER = ComponentMapper.getFor(Attack.class);

    private float damage;
    private float damageDelay;
    private float attackTimer;
    private SoundAsset sfx;

    public Attack (float damage, float damageDelay, SoundAsset soundAsset) {
        this.damage = damage;
        this.damageDelay = damageDelay;
        this.sfx = soundAsset;
        this.attackTimer = 0f;
    }

    public boolean canAttack() {
        return attackTimer == 0;
    }

    public boolean isAttacking() {
        return attackTimer > 0;
    }

    public boolean hasAttackStarted() {
        return MathUtils.isEqual(attackTimer, damageDelay, .0001f);
    }

    public void startAttack() {
        attackTimer = damageDelay;
    }

    public void decreaseAttackTimer(float delta) {
        attackTimer = Math.max(0f, attackTimer - delta);
    }

    public float getDamage() {
        return damage;
    }

    public SoundAsset getSfx() {
        return sfx;
    }
}
