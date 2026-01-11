package com.github.tilcob.game.trigger;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.utils.Timer;
import com.github.tilcob.game.assets.SoundAsset;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.component.Animation2D;
import com.github.tilcob.game.component.Damaged;
import com.github.tilcob.game.config.Constants;

public class TrapTriggerHandler implements TriggerHandler {
    private final AudioManager audioManager;

    public TrapTriggerHandler(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    @Override
    public void execute(Entity trap, Entity triggeringEntity) {
        Animation2D animation2D = Animation2D.MAPPER.get(trap);
        animation2D.setSpeed(Constants.DEFAULT_ANIMATION_SPEED);
        animation2D.setPlayMode(Animation.PlayMode.NORMAL);
        audioManager.playSound(SoundAsset.TRAP);

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                animation2D.setSpeed(0f);
                animation2D.setType(Animation2D.AnimationType.IDLE);
            }
        }, 1.5f);

        triggeringEntity.add(new Damaged(2f));
    }

    @Override
    public void exit(Entity trigger, Entity triggeringEntity) {

    }
}
