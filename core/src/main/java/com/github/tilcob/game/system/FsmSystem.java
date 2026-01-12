package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.github.tilcob.game.component.AnimationFsm;

public class FsmSystem extends IteratingSystem {

    public FsmSystem() {
        super(Family.all(AnimationFsm.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        AnimationFsm.MAPPER.get(entity).getAnimationFsm().update();
    }
}
