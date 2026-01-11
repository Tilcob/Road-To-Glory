package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

public class PlayerReference implements Component {
    public static final ComponentMapper<PlayerReference> MAPPER = ComponentMapper.getFor(PlayerReference.class);

    private Entity player;

    public PlayerReference(Entity player) {
        this.player = player;
    }

    public Entity getPlayer() {
        return player;
    }

    public void setPlayer(Entity player) {
        this.player = player;
    }
}
