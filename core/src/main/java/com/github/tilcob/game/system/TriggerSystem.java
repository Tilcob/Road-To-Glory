package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Timer;
import com.github.tilcob.game.assets.SoundAsset;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.component.*;

public class TriggerSystem extends IteratingSystem {
    private final AudioManager audioManager;

    public TriggerSystem(AudioManager audioManager) {
        super(Family.all(Trigger.class).get());
        this.audioManager = audioManager;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        Trigger trigger = Trigger.MAPPER.get(entity);
        if (trigger.getTriggeringEntity() == null) return;
        fireTrigger(trigger.getName(), trigger.getTriggeringEntity());
        trigger.setTriggeringEntity(null);
    }

    private void fireTrigger(String triggerName, Entity triggeringEntity) {
        switch (triggerName) {
            case "trap_trigger" -> executeTrapScript(triggeringEntity);
            default -> throw new GdxRuntimeException("Unsupported trigger: " + triggerName);
        }
    }

    private void executeTrapScript(Entity triggeringEntity) {
        Entity trapEntity = entityByTiledId(11);
        if (trapEntity == null) return;

        Animation2D animation2D = Animation2D.MAPPER.get(trapEntity);
        animation2D.setSpeed(1f);
        animation2D.setPlayMode(Animation.PlayMode.NORMAL);
        audioManager.playSound(SoundAsset.TRAP);

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                animation2D.setSpeed(0f);
                animation2D.setType(Animation2D.AnimationType.IDLE);
            }
        }, 2.5f);

        triggeringEntity.add(new Damaged(2f));
        System.out.println("Player hurt");
    }

    private Entity entityByTiledId(int id) {
        ImmutableArray<Entity> entities = getEngine().getEntitiesFor(Family.all(Tiled.class).get());
        for (Entity entity : entities) {
            if (Tiled.MAPPER.get(entity).getId() == id) {
                System.out.println("Id: " + id);
                return entity;
            }

        }
        return null;
    }
}
