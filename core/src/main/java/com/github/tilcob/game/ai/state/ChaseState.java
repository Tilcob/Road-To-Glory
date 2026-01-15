package com.github.tilcob.game.ai.state;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.github.tilcob.game.ai.NpcState;
import com.github.tilcob.game.ai.behavior.NpcBehaviorProfile;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.config.Constants;

public class ChaseState implements State<Entity> {

    @Override
    public void enter(Entity entity) {
        Entity player = NpcStateSupport.findPlayer(entity);
        NpcBehaviorProfile behaviorProfile = NpcStateSupport.behaviorProfile(entity);
        if (player == null || !behaviorProfile.canChase() || !NpcStateSupport.canAggro(entity, player)) {
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
        if (player == null || !behaviorProfile.canChase()) {
            if (moveIntent != null) moveIntent.clear();
            NpcFsm.MAPPER.get(entity).getNpcFsm().changeState(NpcState.IDLE);
            return;
        }

        AggroMemory memory = AggroMemory.MAPPER.get(entity);
        if (NpcStateSupport.canAggro(entity, player)) {
            if (moveIntent != null) {
                moveIntent.setTarget(Transform.MAPPER.get(player).getPosition(), Constants.DEFAULT_ARRIVAL_DISTANCE);
            }
            tryAttack(entity, player);
            return;
        }

        if (memory != null) {
            memory.decay(Gdx.graphics.getDeltaTime());
            if (memory.isExpired(behaviorProfile.getAggroForgetTime())) {
                memory.clear();
                if (moveIntent != null) moveIntent.clear();
                NpcFsm.MAPPER.get(entity).getNpcFsm().changeState(shouldReturnToSpawn(entity, behaviorProfile));
                return;
            }
        }

        if (moveIntent != null && memory != null) {
            moveIntent.setTarget(memory.getLastKnownPosition(), Constants.DEFAULT_ARRIVAL_DISTANCE);
        }

        if (behaviorProfile.canReturnToSpawn() && isOutsideLeash(entity, behaviorProfile)) {
            if (moveIntent != null) moveIntent.clear();
            NpcFsm.MAPPER.get(entity).getNpcFsm().changeState(NpcState.RETURN);
            return;
        }
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

    private NpcState shouldReturnToSpawn(Entity entity, NpcBehaviorProfile behaviorProfile) {
        if (behaviorProfile.canReturnToSpawn() && SpawnPoint.MAPPER.get(entity) != null) {
            return NpcState.RETURN;
        }
        return NpcState.IDLE;
    }

    private boolean isOutsideLeash(Entity entity, NpcBehaviorProfile behaviorProfile) {
        if (!behaviorProfile.canReturnToSpawn()) return false;
        SpawnPoint spawnPoint = SpawnPoint.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        if (spawnPoint == null || transform == null) return false;
        float leashRange = behaviorProfile.getLeashRange();
        return transform.getPosition().dst2(spawnPoint.getPosition()) > leashRange * leashRange;
    }

    @Override
    public boolean onMessage(Entity entity, Telegram telegram) {
        return false;
    }
}
