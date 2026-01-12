package com.github.tilcob.game.ai.behavior;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.ai.NpcState;
import com.github.tilcob.game.component.Fsm;

public class EnemyBehaviorProfile extends BaseNpcBehaviorProfile {

    @Override
    public void updateIdle(Entity entity) {
        Entity player = findPlayer(entity);
        if (player != null && inAggroRange(entity, player, getAggroRange())) {
            Fsm.MAPPER.get(entity).getNpcFsm().changeState(NpcState.CHASE);
        }
    }
}
