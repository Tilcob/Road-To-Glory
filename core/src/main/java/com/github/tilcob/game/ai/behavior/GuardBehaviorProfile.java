package com.github.tilcob.game.ai.behavior;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.ai.NpcState;
import com.github.tilcob.game.ai.state.NpcStateSupport;
import com.github.tilcob.game.component.NpcFsm;
import com.github.tilcob.game.component.PatrolRoute;
import com.github.tilcob.game.config.Constants;

public class GuardBehaviorProfile extends BaseNpcBehaviorProfile {

    public GuardBehaviorProfile() {
        super(Constants.AGGRO_RANGE,
            Constants.HEARING_RANGE,
            Constants.AGGRO_FORGET_TIME,
            Constants.LEASH_RANGE,
            true,
            false,
            true,
            true,
            true,
            NpcState.PATROL);
    }

    @Override
    public void updateIdle(Entity entity) {
        if (canChase() && NpcStateSupport.canAggro(entity, findPlayer(entity))) {
            NpcFsm.MAPPER.get(entity).getNpcFsm().changeState(NpcState.CHASE);
            return;
        }

        PatrolRoute route = PatrolRoute.MAPPER.get(entity);
        if (canPatrol() && route != null && !route.getPoints().isEmpty()) {
            NpcFsm.MAPPER.get(entity).getNpcFsm().changeState(NpcState.PATROL);
        }
    }
}
