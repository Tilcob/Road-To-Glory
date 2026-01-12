package com.github.tilcob.game.ai.state;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.math.MathUtils;
import com.github.tilcob.game.ai.NpcState;
import com.github.tilcob.game.component.Controller;
import com.github.tilcob.game.component.MoveIntent;
import com.github.tilcob.game.component.NpcFsm;
import com.github.tilcob.game.input.Command;

public class WanderState implements State<Entity> {
    @Override
    public void enter(Entity entity) {
        MoveIntent moveIntent = MoveIntent.MAPPER.get(entity);
        if (moveIntent == null) return;
        int direction = MathUtils.random(1, 4);
        switch (direction) {
            case 1 -> moveIntent.setDirection(0f, 1f);
            case 2 -> moveIntent.setDirection(0f, -1f);
            case 3 -> moveIntent.setDirection(-1f, 0f);
            case 4 -> moveIntent.setDirection(1f, 0f);
        }
    }

    @Override
    public void update(Entity entity) {
        if (Math.random() < 0.01) {
            MoveIntent moveIntent = MoveIntent.MAPPER.get(entity);
            if (moveIntent != null) moveIntent.clear();
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
