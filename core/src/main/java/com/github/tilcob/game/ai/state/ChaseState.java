package com.github.tilcob.game.ai.state;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.github.tilcob.game.ai.NpcState;
import com.github.tilcob.game.ai.behavior.NpcBehaviorProfile;
import com.github.tilcob.game.component.MoveIntent;
import com.github.tilcob.game.component.NpcFsm;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.component.Attack;

public class ChaseState implements State<Entity> {

    @Override
    public void enter(Entity entity) {
        Entity player = NpcStateSupport.findPlayer(entity);
        NpcBehaviorProfile behaviorProfile = NpcStateSupport.behaviorProfile(entity);
        if (player == null || !NpcStateSupport.inAggroRange(entity, player, behaviorProfile.getAggroRange())) {
            NpcFsm.MAPPER.get(entity).getNpcFsm().changeState(NpcState.IDLE);
            return;
        }
        MoveIntent moveIntent = MoveIntent.MAPPER.get(entity);
        if (moveIntent == null) return;
        moveIntent.setTarget(Transform.MAPPER.get(player).getPosition(), Constants.DEFAULT_ARRIVAL_DISTANCE);
    }

    @Override
    public void update(Entity entity) {
        Entity player = NpcStateSupport.findPlayer(entity);
        MoveIntent moveIntent = MoveIntent.MAPPER.get(entity);
        NpcBehaviorProfile behaviorProfile = NpcStateSupport.behaviorProfile(entity);
        if (player == null || !NpcStateSupport.inAggroRange(entity, player, behaviorProfile.getAggroRange())) {
            if (moveIntent != null) moveIntent.clear();
            NpcFsm.MAPPER.get(entity).getNpcFsm().changeState(NpcState.IDLE);
            return;
        }
        if (moveIntent != null) {
            moveIntent.setTarget(Transform.MAPPER.get(player).getPosition(), Constants.DEFAULT_ARRIVAL_DISTANCE);
        }
        tryAttack(entity, player);
    }

    @Override
    public void exit(Entity entity) {
        MoveIntent moveIntent = MoveIntent.MAPPER.get(entity);
        if (moveIntent != null) moveIntent.clear();
    }

    private void tryAttack(Entity entity, Entity player) {
        Attack attack = Attack.MAPPER.get(entity);
        if (attack == null || !attack.canAttack()) {
            return;
        }
        Transform playerTransform = Transform.MAPPER.get(player);
        Transform selfTransform = Transform.MAPPER.get(entity);
        if (playerTransform == null || selfTransform == null) {
            return;
        }
        float distance = playerTransform.getPosition().dst(selfTransform.getPosition());
        if (distance <= Constants.ENEMY_ATTACK_RANGE) {
            attack.startAttack();
            MoveIntent moveIntent = MoveIntent.MAPPER.get(entity);
            if (moveIntent != null) {
                moveIntent.clear();
            }
        }
    }

    @Override
    public boolean onMessage(Entity entity, Telegram telegram) {
        return false;
    }
}
