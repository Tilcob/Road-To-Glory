package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.component.Attack;
import com.github.tilcob.game.component.Facing;
import com.github.tilcob.game.component.Move;
import com.github.tilcob.game.component.Physic;
import com.github.tilcob.game.event.CommandEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.input.Command;

public class AttackSystem extends IteratingSystem implements Disposable {
    private final GameEventBus eventBus;
    private final AudioManager audioManager;

    public AttackSystem(AudioManager audioManager, GameEventBus eventBus) {
        super(Family.all(Attack.class, Facing.class, Physic.class).get());
        this.audioManager = audioManager;
        this.eventBus = eventBus;

        eventBus.subscribe(CommandEvent.class, this::onCommand);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Attack attack = Attack.MAPPER.get(entity);

        if (attack.consumeStarted()) {
            if (attack.getSfx() != null) audioManager.playSound(attack.getSfx());
            setRooted(entity, true);
        }

        if (attack.canAttack()) return;

        attack.advance(deltaTime);
        if (attack.consumeTriggered()) {
            setRooted(entity, false);
        }

        if (attack.consumeFinished()) {
            setRooted(entity, false);
        }
    }

    private void onCommand(CommandEvent event) {
        if (event.isHandled()) return;
        if (event.getCommand() != Command.SELECT) return;
        Attack attack = Attack.MAPPER.get(event.getPlayer());
        if (attack == null) return;
        if (attack.canAttack()) {
            attack.startAttack();
            event.setHandled(true);
        }
    }

    private static void setRooted(Entity entity, boolean rooted) {
        Move move = Move.MAPPER.get(entity);
        if (move != null) {
            move.setRooted(rooted);
        }
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(CommandEvent.class, this::onCommand);
    }
}
