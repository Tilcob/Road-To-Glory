package com.github.tilcob.game.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.github.tilcob.game.ai.state.*;

public enum NpcState implements State<Entity> {
    IDLE(new IdleState()),
    TALKING(new TalkingState()),
    WANDER(new WanderState()),
    CHASE(new ChaseState()),
    PATROL(new PatrolState()),
    RETURN(new ReturnState()),;

    private final State<Entity> delegate;

    NpcState(State<Entity> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void enter(Entity entity) {
        delegate.enter(entity);
    }

    @Override
    public void update(Entity entity) {
        delegate.update(entity);
    }

    @Override
    public void exit(Entity entity) {
        delegate.exit(entity);
    }

    @Override
    public boolean onMessage(Entity entity, Telegram telegram) {
        return delegate.onMessage(entity, telegram);
    }
}
