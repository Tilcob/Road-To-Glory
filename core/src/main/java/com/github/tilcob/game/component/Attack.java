package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.MathUtils;
import com.github.tilcob.game.assets.SoundAsset;

public class Attack implements Component {
    public static final ComponentMapper<Attack> MAPPER = ComponentMapper.getFor(Attack.class);

    private final float damage;
    private final float windup;
    private final float cooldown;
    private float attackTimer;
    private SoundAsset sfx;
    private boolean startedThisFrame;
    private boolean finishedThisFrame;
    private State state;

    public Attack (float damage, float windup, float cooldown, SoundAsset soundAsset) {
        this.damage = damage;
        this.windup = windup;
        this.cooldown = cooldown;
        this.sfx = soundAsset;
        this.attackTimer = 0f;
        this.startedThisFrame = false;
        this.finishedThisFrame = false;
        this.state = State.IDLE;
    }

    public boolean canAttack() {
        return state == State.IDLE;
    }

    public boolean isAttacking() {
        return state != State.IDLE;
    }

    public boolean isInWindup() {
        return state == State.WINDUP;
    }

    public boolean isInRecovery() {
        return state == State.RECOVERY;
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
    }

    public boolean advance(float delta) {
        boolean triggered = false;
        finishedThisFrame = false;

        if (state == State.WINDUP) {
            attackTimer = Math.max(0f, attackTimer - delta);
            if (attackTimer == 0f) {
                triggered = true;
                if (cooldown > 0f) {
                    state = State.RECOVERY;
                    attackTimer = cooldown;
                } else {
                    state = State.IDLE;
                    finishedThisFrame = true;
                }
            }
        } else if (state == State.RECOVERY) {
            attackTimer = Math.max(0f, attackTimer - delta);
            if (attackTimer == 0f) {
                state = State.IDLE;
                finishedThisFrame = true;
            }
        }

        return triggered;
    }

    public boolean consumeFinished() {
        if (!finishedThisFrame) return false;
        finishedThisFrame = false;
        return true;
    }

    public float getDamage() {
        return damage;
    }

    public SoundAsset getSfx() {
        return sfx;
    }

    public enum State {
        IDLE,
        WINDUP,
        RECOVERY
    }
}
