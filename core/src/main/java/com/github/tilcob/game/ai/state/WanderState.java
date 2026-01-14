package com.github.tilcob.game.ai.state;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.MathUtils;
import com.github.tilcob.game.ai.NpcState;
import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.component.MoveIntent;
import com.github.tilcob.game.component.NpcFsm;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.config.Constants;

public class WanderState implements State<Entity> {
    @Override
    public void enter(Entity entity) {
        MoveIntent moveIntent = MoveIntent.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        if (moveIntent == null || transform == null) return;

        Vector2 position = transform.getPosition();
        float radius = Constants.WANDER_RADIUS;
        float offsetX = MathUtils.random(-radius, radius);
        float offsetY = MathUtils.random(-radius, radius);
        if (offsetX == 0f && offsetY == 0f) {
            offsetX = radius;
        }
        Vector2 target = new Vector2(position.x + offsetX, position.y + offsetY);
        moveIntent.setTarget(target, Constants.DEFAULT_ARRIVAL_DISTANCE);
    }

    @Override
    public void update(Entity entity) {
        MoveIntent moveIntent = MoveIntent.MAPPER.get(entity);
        if (moveIntent == null || moveIntent.isActive()) {
            return;
        }
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
