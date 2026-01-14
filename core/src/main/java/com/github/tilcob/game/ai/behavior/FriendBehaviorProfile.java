package com.github.tilcob.game.ai.behavior;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.github.tilcob.game.ai.NpcState;
import com.github.tilcob.game.component.NpcFsm;
import com.github.tilcob.game.component.WanderTimer;
import com.github.tilcob.game.config.Constants;

public class FriendBehaviorProfile extends BaseNpcBehaviorProfile {

    public FriendBehaviorProfile() {
        super(Constants.AGGRO_RANGE,
            Constants.HEARING_RANGE,
            Constants.AGGRO_FORGET_TIME,
            Constants.LEASH_RANGE,
            false,
            true,
            false,
            false,
            false,
            NpcState.IDLE);
    }

    @Override
    public void updateIdle(Entity entity) {
        if (!canWander()) return;
        WanderTimer wanderTimer = WanderTimer.MAPPER.get(entity);
        if (wanderTimer == null) {
            NpcFsm.MAPPER.get(entity).getNpcFsm().changeState(NpcState.WANDER);
            return;
        }

        wanderTimer.decreaseTimer(Gdx.graphics.getDeltaTime());
        if (wanderTimer.getTimer() > 0) return;

        wanderTimer.resetTimer();
        NpcFsm.MAPPER.get(entity).getNpcFsm().changeState(NpcState.WANDER);
    }
}
