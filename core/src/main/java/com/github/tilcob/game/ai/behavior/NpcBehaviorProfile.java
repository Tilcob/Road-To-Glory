package com.github.tilcob.game.ai.behavior;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.ai.NpcState;

public interface NpcBehaviorProfile {
    void updateIdle(Entity entity);
    float getAggroRange();
    NpcState getInitialNpcState();
}
