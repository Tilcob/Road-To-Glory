package com.github.tilcob.game.ai.behavior;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.github.tilcob.game.ai.NpcState;
import com.github.tilcob.game.component.NpcFsm;
import com.github.tilcob.game.component.WanderTimer;

public class FriendBehaviorProfile extends BaseNpcBehaviorProfile {

    @Override
    public void updateIdle(Entity entity) {
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
