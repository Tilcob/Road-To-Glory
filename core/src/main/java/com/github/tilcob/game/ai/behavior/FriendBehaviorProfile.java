package com.github.tilcob.game.ai.behavior;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.ai.NpcState;
import com.github.tilcob.game.component.NpcFsm;

public class FriendBehaviorProfile extends BaseNpcBehaviorProfile {

    @Override
    public void updateIdle(Entity entity) {
        NpcFsm.MAPPER.get(entity).getNpcFsm().changeState(NpcState.WANDER);
    }
}
