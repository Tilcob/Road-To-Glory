package com.github.tilcob.game.ai.state;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.ai.NpcState;
import com.github.tilcob.game.component.MoveIntent;
import com.github.tilcob.game.component.NpcFsm;
import com.github.tilcob.game.component.PatrolRoute;
import com.github.tilcob.game.config.Constants;

public class PatrolState implements State<Entity> {
    @Override
    public void enter(Entity entity) {
        PatrolRoute route = PatrolRoute.MAPPER.get(entity);
        if (route == null || route.getPoints().isEmpty()) {
            NpcFsm.MAPPER.get(entity).getNpcFsm().changeState(NpcState.IDLE);
            return;
        }
        MoveIntent moveIntent = MoveIntent.MAPPER.get(entity);
        Vector2 target = route.getCurrentPoint();
        if (moveIntent != null && target != null) {
            moveIntent.setTarget(target, Constants.DEFAULT_ARRIVAL_DISTANCE);
        }
    }

    @Override
    public void update(Entity entity) {
        PatrolRoute route = PatrolRoute.MAPPER.get(entity);
        if (route == null || route.getPoints().isEmpty()) {
            NpcFsm.MAPPER.get(entity).getNpcFsm().changeState(NpcState.IDLE);
            return;
        }

        if (route.isWaiting()) {
            route.updateWaitTimer(Gdx.graphics.getDeltaTime());
            return;
        }

        MoveIntent moveIntent = MoveIntent.MAPPER.get(entity);
        if (moveIntent == null || moveIntent.isActive()) return;

        route.setWaitTimer();
        Vector2 next = route.advance();
        if (next != null) moveIntent.setTarget(next, Constants.DEFAULT_ARRIVAL_DISTANCE);
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
