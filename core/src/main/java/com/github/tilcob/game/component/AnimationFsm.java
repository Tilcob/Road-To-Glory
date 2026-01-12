package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.github.tilcob.game.ai.AnimationState;
import com.github.tilcob.game.ai.NpcState;

public class AnimationFsm implements Component {
    public static final ComponentMapper<AnimationFsm> MAPPER = ComponentMapper.getFor(AnimationFsm.class);

    private final DefaultStateMachine<Entity, AnimationState> animationFsm;

    public AnimationFsm(Entity owner) {
        this.animationFsm = new DefaultStateMachine<>(owner);
        animationFsm.setInitialState(AnimationState.IDLE);
    }

    public DefaultStateMachine<Entity, AnimationState> getAnimationFsm() {
        return animationFsm;
    }
}
