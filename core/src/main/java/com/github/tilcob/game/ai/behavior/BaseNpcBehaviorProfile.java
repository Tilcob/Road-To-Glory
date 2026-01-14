package com.github.tilcob.game.ai.behavior;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.ai.NpcState;
import com.github.tilcob.game.ai.state.NpcStateSupport;
import com.github.tilcob.game.config.Constants;

public abstract class BaseNpcBehaviorProfile implements NpcBehaviorProfile {
    private final float aggroRange;
    private final float hearingRange;
    private final float aggroForgetTime;
    private final float leashRange;
    private final boolean canChase;
    private final boolean canWander;
    private final boolean canPatrol;
    private final boolean canCallForHelp;
    private final boolean canReturnToSpawn;
    private final NpcState initialState;

    protected BaseNpcBehaviorProfile() {
        this(Constants.AGGRO_RANGE,
            Constants.HEARING_RANGE,
            Constants.AGGRO_FORGET_TIME,
            Constants.LEASH_RANGE,
            false,
            false,
            false,
            false,
            false,
            NpcState.IDLE);
    }

    protected BaseNpcBehaviorProfile(float aggroRange,
                                     float hearingRange,
                                     float aggroForgetTime,
                                     float leashRange,
                                     boolean canChase,
                                     boolean canWander,
                                     boolean canPatrol,
                                     boolean canCallForHelp,
                                     boolean canReturnToSpawn,
                                     NpcState initialState) {
        this.aggroRange = aggroRange;
        this.hearingRange = hearingRange;
        this.aggroForgetTime = aggroForgetTime;
        this.leashRange = leashRange;
        this.canChase = canChase;
        this.canWander = canWander;
        this.canPatrol = canPatrol;
        this.canCallForHelp = canCallForHelp;
        this.canReturnToSpawn = canReturnToSpawn;
        this.initialState = initialState;
    }

    @Override
    public float getAggroRange() {
        return aggroRange;
    }

    @Override
    public float getHearingRange() {
        return hearingRange;
    }

    @Override
    public float getAggroForgetTime() {
        return aggroForgetTime;
    }

    @Override
    public float getLeashRange() {
        return leashRange;
    }

    @Override
    public boolean canChase() {
        return canChase;
    }

    @Override
    public boolean canWander() {
        return canWander;
    }

    @Override
    public boolean canPatrol() {
        return canPatrol;
    }

    @Override
    public boolean canCallForHelp() {
        return canCallForHelp;
    }

    @Override
    public boolean canReturnToSpawn() {
        return canReturnToSpawn;
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
