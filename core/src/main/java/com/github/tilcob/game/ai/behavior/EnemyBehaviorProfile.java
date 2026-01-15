package com.github.tilcob.game.ai.behavior;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.ai.NpcState;
import com.github.tilcob.game.ai.state.NpcStateSupport;
import com.github.tilcob.game.component.NpcFsm;
import com.github.tilcob.game.config.Constants;

public class EnemyBehaviorProfile extends BaseNpcBehaviorProfile {

    public EnemyBehaviorProfile() {
        super(Constants.AGGRO_RANGE,
            Constants.HEARING_RANGE,
            Constants.AGGRO_FORGET_TIME,
            Constants.LEASH_RANGE,
            true,
            false,
            false,
            true,
            true,
            NpcState.IDLE);
    }

    @Override
    public void updateIdle(Entity entity) {
        if (!canChase()) return;
        Entity player = findPlayer(entity);
        if (NpcStateSupport.canAggro(entity, player)) {
            NpcFsm.MAPPER.get(entity).getNpcFsm().changeState(NpcState.CHASE);
        }
    }
}
