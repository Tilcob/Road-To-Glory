package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

public class OpenChestRequest implements Component {
    public static final ComponentMapper<OpenChestRequest> MAPPER = ComponentMapper.getFor(OpenChestRequest.class);

    private final Entity chest;

    public OpenChestRequest(Entity chest) {
        this.chest = chest;
    }

    public Entity getChest() {
        return chest;
    }
}
