package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.github.tilcob.game.ai.AnimationState;
import com.github.tilcob.game.ai.NpcState;

public class Fsm implements Component {
    public static final ComponentMapper<Fsm> MAPPER = ComponentMapper.getFor(Fsm.class);

    private final DefaultStateMachine<Entity, AnimationState> animationFsm;
    private final DefaultStateMachine<Entity, NpcState> npcFsm;

    public Fsm(Entity owner) {
        this.animationFsm = new DefaultStateMachine<>(owner);
        this.npcFsm = new DefaultStateMachine<>(owner);
        animationFsm.setInitialState(AnimationState.IDLE);
        npcFsm.setInitialState(NpcState.IDLE);
    }

    public DefaultStateMachine<Entity, AnimationState> getAnimationFsm() {
        return animationFsm;
    }

    public DefaultStateMachine<Entity, NpcState> getNpcFsm() {
        return npcFsm;
    }
}
