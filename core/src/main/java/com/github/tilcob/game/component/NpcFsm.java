package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.github.tilcob.game.ai.NpcState;
import com.github.tilcob.game.ai.behavior.NpcBehaviorRegistry;

public class NpcFsm implements Component {
    public static final ComponentMapper<NpcFsm> MAPPER = ComponentMapper.getFor(NpcFsm.class);

    private final DefaultStateMachine<Entity, NpcState> npcFsm;

    public NpcFsm(Entity owner) {
        this.npcFsm = new DefaultStateMachine<>(owner);
        Npc npc = Npc.MAPPER.get(owner);
        if (npc == null) {
            npcFsm.setInitialState(NpcState.IDLE);
            return;
        }
        npcFsm.setInitialState(NpcBehaviorRegistry.get(npc.getType()).getInitialNpcState());
    }

    public DefaultStateMachine<Entity, NpcState> getNpcFsm() {
        return npcFsm;
    }
}
