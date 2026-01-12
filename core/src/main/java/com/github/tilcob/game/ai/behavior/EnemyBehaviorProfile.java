package com.github.tilcob.game.ai.behavior;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.ai.NpcState;
import com.github.tilcob.game.component.AnimationFsm;
import com.github.tilcob.game.component.NpcFsm;

public class EnemyBehaviorProfile extends BaseNpcBehaviorProfile {

    @Override
    public void updateIdle(Entity entity) {
        Entity player = findPlayer(entity);
        if (player != null && inAggroRange(entity, player, getAggroRange())) {
            NpcFsm.MAPPER.get(entity).getNpcFsm().changeState(NpcState.CHASE);
        }
    }
}
