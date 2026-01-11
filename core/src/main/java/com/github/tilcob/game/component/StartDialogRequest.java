package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

public class StartDialogRequest implements Component {
    public static final ComponentMapper<StartDialogRequest> MAPPER = ComponentMapper.getFor(StartDialogRequest.class);

    private final Entity npc;

    public StartDialogRequest(Entity npc) {
        this.npc = npc;
    }

    public Entity getNpc() {
        return npc;
    }
}
