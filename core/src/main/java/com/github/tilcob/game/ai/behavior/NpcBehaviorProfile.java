package com.github.tilcob.game.ai.behavior;

import com.badlogic.ashley.core.Entity;

public interface NpcBehaviorProfile {
    void updateIdle(Entity entity);
    float getAggroRange();
}
