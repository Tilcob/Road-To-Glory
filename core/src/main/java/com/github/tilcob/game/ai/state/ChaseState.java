package com.github.tilcob.game.ai.state;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.github.tilcob.game.ai.NpcState;
import com.github.tilcob.game.ai.behavior.NpcBehaviorProfile;
import com.github.tilcob.game.component.AnimationFsm;
import com.github.tilcob.game.component.NpcFsm;

public class ChaseState implements State<Entity> {

    @Override
    public void enter(Entity entity) {
        Entity player = NpcStateSupport.findPlayer(entity);
        NpcBehaviorProfile behaviorProfile = NpcStateSupport.behaviorProfile(entity);
        if (player == null || !NpcStateSupport.inAggroRange(entity, player, behaviorProfile.getAggroRange())) {
            NpcFsm.MAPPER.get(entity).getNpcFsm().changeState(NpcState.IDLE);
            return;
        }
        NpcStateSupport.chasePlayer(entity, player);
    }

    @Override
    public void update(Entity entity) {

    }

    @Override
    public void exit(Entity entity) {

    }

    @Override
    public boolean onMessage(Entity entity, Telegram telegram) {
        return false;
    }
}
