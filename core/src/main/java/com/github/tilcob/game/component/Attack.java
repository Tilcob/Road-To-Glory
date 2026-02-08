package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.github.tilcob.game.assets.SoundAsset;
import com.github.tilcob.game.config.Constants;

public class Attack implements Component {
    public static final ComponentMapper<Attack> MAPPER = ComponentMapper.getFor(Attack.class);

    private final float baseDamage;
    private final float hitDelay;
    private float damage;
    private float windup;
    private float recovery;
    private float attackTimer;
    private float damageTimer;
    private SoundAsset sfx;
    private boolean startedThisFrame;
    private boolean triggeredThisFrame;
    private boolean finishedThisFrame;
    private State state;

    public Attack (float damage, float windup, float cooldown, float hitDelay, SoundAsset soundAsset) {
        this.damage = damage;
        this.hitDelay = Math.max(0, hitDelay);
        this.baseDamage = damage;
        this.windup = windup;
        this.recovery = Math.max(cooldown, Constants.FIXED_INTERVAL);
        this.sfx = soundAsset;
        this.attackTimer = 0f;
        this.damageTimer = -1f;
        this.startedThisFrame = false;
        this.triggeredThisFrame = false;
        this.finishedThisFrame = false;
        this.state = State.IDLE;
    }

    public boolean canAttack() {
        return state == State.IDLE;
    }

    public boolean isInWindup() {
        return state == State.WINDUP;
    }

    public boolean isInRecovery() {
        return state == State.RECOVERY;
    }

    public void setWindup(float windup) {
        this.windup = windup;
        if (state == State.WINDUP) {
            attackTimer = Math.max(0f, windup);
        }
    }

    public boolean consumeStarted() {
        if (!startedThisFrame) return false;
        startedThisFrame = false;
        return true;
    }

    public void startAttack() {
        if (!canAttack()) return;
        state = State.WINDUP;
        attackTimer = Math.max(0f, windup);
        startedThisFrame = true;
        damageTimer = hitDelay;
    }

    public void advance(float delta) {
        finishedThisFrame = false;
        triggeredThisFrame = false;

        if (state != State.IDLE && damageTimer >= 0f) {
            damageTimer = Math.max(0f, damageTimer - delta);
            if (damageTimer == 0f) {
                triggeredThisFrame = true;
                damageTimer = -1f;
            }
        }

        if (state == State.WINDUP) {
            attackTimer = Math.max(0f, attackTimer - delta);
            if (attackTimer == 0f) {
                state = State.RECOVERY;
                attackTimer = Math.max(recovery, Constants.FIXED_INTERVAL);
            }
        } else if (state == State.RECOVERY) {
            attackTimer = Math.max(0f, attackTimer - delta);
            if (attackTimer == 0f) {
                state = State.IDLE;
                finishedThisFrame = true;
            }
        }
    }

    public boolean consumeTriggered() {
        if (!triggeredThisFrame) return false;
        triggeredThisFrame = false;
        return true;
    }

    public boolean consumeFinished() {
        if (!finishedThisFrame) return false;
        finishedThisFrame = false;
        return true;
    }

    public float getBaseDamage() {
        return baseDamage;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public SoundAsset getSfx() {
        return sfx;
    }

    public float getRecovery() {
        return recovery;
    }

    public void setRecovery(float recovery) {
        this.recovery = Math.max(recovery, Constants.FIXED_INTERVAL);
        if (state == State.RECOVERY) {
            attackTimer = Math.max(0f, this.recovery);
        }
    }

    public enum State {
        IDLE,
        WINDUP,
        RECOVERY
    }
}
