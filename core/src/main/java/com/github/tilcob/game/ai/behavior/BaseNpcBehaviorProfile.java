package com.github.tilcob.game.ai.behavior;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.ai.NpcState;
import com.github.tilcob.game.ai.state.NpcStateSupport;
import com.github.tilcob.game.config.Constants;

public abstract class BaseNpcBehaviorProfile implements NpcBehaviorProfile {
    private final float aggroRange;
    private final NpcState initialState;

    protected BaseNpcBehaviorProfile() {
        this(Constants.AGGRO_RANGE, NpcState.IDLE);
    }

    protected BaseNpcBehaviorProfile(float aggroRange, NpcState initialState) {
        this.aggroRange = aggroRange;
        this.initialState = initialState;
    }

    @Override
    public float getAggroRange() {
        return aggroRange;
    }

    @Override
    public NpcState getInitialNpcState() {
        return initialState;
    }

    protected Entity findPlayer(Entity entity) {
        return NpcStateSupport.findPlayer(entity);
    }

    protected boolean inAggroRange(Entity entity, Entity player, float aggroRange) {
        return  NpcStateSupport.inAggroRange(entity, player, aggroRange);
    }
}
