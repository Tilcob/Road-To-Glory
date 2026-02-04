package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class NpcRole implements Component {
    public static final ComponentMapper<NpcRole> MAPPER = ComponentMapper.getFor(NpcRole.class);

    private Role role;

    public NpcRole(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public enum Role {
        QUEST_GIVER,
        MERCHANT,
        DANGER,
        INFO,
        TRAINER,
    }
}
