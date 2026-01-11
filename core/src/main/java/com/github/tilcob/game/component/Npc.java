package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.github.tilcob.game.npc.NpcType;

public class Npc implements Component {
    public static final ComponentMapper<Npc> MAPPER = ComponentMapper.getFor(Npc.class);

    private NpcType type;
    private final String name;

    public Npc(NpcType type, String name) {
        this.type = type;
        this.name = name;
    }

    public NpcType getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
