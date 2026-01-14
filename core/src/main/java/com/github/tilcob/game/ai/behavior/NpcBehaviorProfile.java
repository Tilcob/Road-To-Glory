package com.github.tilcob.game.ai.behavior;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.ai.NpcState;

public interface NpcBehaviorProfile {
    void updateIdle(Entity entity);
    float getAggroRange();
    float getHearingRange();
    float getAggroForgetTime();
    float getLeashRange();
    boolean canChase();
    boolean canWander();
    boolean canPatrol();
    boolean canCallForHelp();
    boolean canReturnToSpawn();
    NpcState getInitialNpcState();
}
