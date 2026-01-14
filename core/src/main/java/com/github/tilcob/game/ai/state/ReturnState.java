package com.github.tilcob.game.ai.state;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.github.tilcob.game.ai.NpcState;
import com.github.tilcob.game.ai.behavior.NpcBehaviorProfile;
import com.github.tilcob.game.component.MoveIntent;
import com.github.tilcob.game.component.NpcFsm;
import com.github.tilcob.game.component.SpawnPoint;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.config.Constants;

public class ReturnState implements State<Entity> {
    @Override
    public void enter(Entity entity) {
        SpawnPoint spawnPoint = SpawnPoint.MAPPER.get(entity);
        if (spawnPoint == null) {
            NpcFsm.MAPPER.get(entity).getNpcFsm().changeState(NpcState.IDLE);
            return;
        }
        MoveIntent moveIntent = MoveIntent.MAPPER.get(entity);
        if (moveIntent != null) {
            moveIntent.setTarget(spawnPoint.getPosition(), Constants.DEFAULT_ARRIVAL_DISTANCE);
        }
    }

    @Override
    public void update(Entity entity) {
        SpawnPoint spawnPoint = SpawnPoint.MAPPER.get(entity);
        if (spawnPoint == null) {
            NpcFsm.MAPPER.get(entity).getNpcFsm().changeState(NpcState.IDLE);
            return;
        }

        NpcBehaviorProfile profile = NpcStateSupport.behaviorProfile(entity);
        if (profile.canChase() && NpcStateSupport.canAggro(entity, NpcStateSupport.findPlayer(entity))) {
            NpcFsm.MAPPER.get(entity).getNpcFsm().changeState(NpcState.CHASE);
            return;
        }

        MoveIntent moveIntent = MoveIntent.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        if (moveIntent == null || transform == null) {
            NpcFsm.MAPPER.get(entity).getNpcFsm().changeState(NpcState.IDLE);
            return;
        }

        if (transform.getPosition().dst2(spawnPoint.getPosition())
            <= Constants.DEFAULT_ARRIVAL_DISTANCE * Constants.DEFAULT_ARRIVAL_DISTANCE) {
            moveIntent.clear();
            NpcFsm.MAPPER.get(entity).getNpcFsm().changeState(NpcState.IDLE);
        }
    }

    @Override
    public void exit(Entity entity) {
        MoveIntent moveIntent = MoveIntent.MAPPER.get(entity);
        if (moveIntent != null) moveIntent.clear();
    }

    @Override
    public boolean onMessage(Entity entity, Telegram telegram) {
        return false;
    }
}
