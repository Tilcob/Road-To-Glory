package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.github.tilcob.game.ai.NpcState;

public class NpcFsm implements Component {
    public static final ComponentMapper<NpcFsm> MAPPER = ComponentMapper.getFor(NpcFsm.class);

    private final DefaultStateMachine<Entity, NpcState> npcFsm;

    public NpcFsm(Entity owner) {
        this.npcFsm = new DefaultStateMachine<>(owner);
        npcFsm.setInitialState(NpcState.IDLE);
    }

    public DefaultStateMachine<Entity, NpcState> getNpcFsm() {
        return npcFsm;
    }
}
