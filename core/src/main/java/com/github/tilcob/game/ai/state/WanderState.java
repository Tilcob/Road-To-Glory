package com.github.tilcob.game.ai.state;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.ai.NpcState;
import com.github.tilcob.game.component.MoveIntent;
import com.github.tilcob.game.component.NpcFsm;
import com.github.tilcob.game.component.SpawnPoint;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.config.Constants;

public class WanderState implements State<Entity> {
    @Override
    public void enter(Entity entity) {
        MoveIntent moveIntent = MoveIntent.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        SpawnPoint spawnPoint = SpawnPoint.MAPPER.get(entity);
        if (moveIntent == null || transform == null) return;

        Vector2 position = spawnPoint != null ? spawnPoint.getPosition() : transform.getPosition();
        moveIntent.setTarget(calcWanderPosition(position), Constants.DEFAULT_ARRIVAL_DISTANCE);
    }

    private Vector2 calcWanderPosition(Vector2 position) {
        float angle = MathUtils.random(0f, MathUtils.PI2);
        float dist = Constants.WANDER_RADIUS * (float) Math.sqrt(MathUtils.random());
        float offsetX = MathUtils.cos(angle) * dist;
        float offsetY = MathUtils.sin(angle) * dist;

        return new Vector2(position.x + offsetX, position.y + offsetY);
    }

    @Override
    public void update(Entity entity) {
        MoveIntent moveIntent = MoveIntent.MAPPER.get(entity);
        if (moveIntent == null || moveIntent.isActive()) return;
        NpcFsm.MAPPER.get(entity).getNpcFsm().changeState(NpcState.IDLE);
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
