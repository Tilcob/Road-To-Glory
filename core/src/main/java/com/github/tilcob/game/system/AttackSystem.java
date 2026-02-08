package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.ability.Ability;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.event.AbilityRequestEvent;
import com.github.tilcob.game.event.CommandEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.input.Command;

public class AttackSystem extends IteratingSystem implements Disposable {
    private final GameEventBus eventBus;
    private final AudioManager audioManager;

    public AttackSystem(AudioManager audioManager, GameEventBus eventBus) {
        super(Family.all(Attack.class).get());
        this.audioManager = audioManager;
        this.eventBus = eventBus;

        eventBus.subscribe(AbilityRequestEvent.class, this::onAbilityRequest);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Attack attack = Attack.MAPPER.get(entity);

        if (attack.consumeStarted()) {
            if (attack.getSfx() != null) audioManager.playSound(attack.getSfx());
            setLocks(entity, true);
        }

        if (attack.canAttack()) return;

        attack.advance(deltaTime);
        attack.consumeTriggered();

        if (attack.consumeFinished()) {
            setLocks(entity, false);
        }
    }

    private void onAbilityRequest(AbilityRequestEvent event) {
        if (event.isHandled()) return;
        if (!event.isResolved()) return;
        if (event.getAbility() != Ability.ATTACK) return;
        Attack attack = Attack.MAPPER.get(event.getEntity());
        if (attack == null) return;
        if (attack.canAttack()) {
            attack.startAttack();
            event.setHandled(true);
        }
    }

    private static void setLocks(Entity entity, boolean locked) {
        ActionLock lock = ActionLock.MAPPER.get(entity);
        if (lock == null) {
            lock = new ActionLock();
            entity.add(lock);
        }
        lock.setLockMovement(locked);
        lock.setLockFacing(locked);
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(AbilityRequestEvent.class, this::onAbilityRequest);
    }
}
