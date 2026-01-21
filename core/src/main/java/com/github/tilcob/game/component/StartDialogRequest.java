package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

public class StartDialogRequest implements Component {
    public static final ComponentMapper<StartDialogRequest> MAPPER = ComponentMapper.getFor(StartDialogRequest.class);

    private final Entity npc;
    private final String nodeId;

    public StartDialogRequest(Entity npc) {
        this(npc, null);
    }

    public StartDialogRequest(Entity npc, String nodeId) {
        this.npc = npc;
        this.nodeId = nodeId;
    }

    public Entity getNpc() {
        return npc;
    }

    public String getNodeId() {
        return nodeId;
    }
}
