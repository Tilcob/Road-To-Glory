package com.github.tilcob.game.ai.state;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;

public class IdleState implements State<Entity> {
    @Override
    public void enter(Entity entity) {
    }

    @Override
    public void update(Entity entity) {
        NpcStateSupport.behaviorProfile(entity).updateIdle(entity);
    }

    @Override
    public void exit(Entity entity) {
    }

    @Override
    public boolean onMessage(Entity entity, Telegram telegram) {
        return false;
    }
}
