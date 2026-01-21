package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class PlayerInputLock implements Component {
    public static final ComponentMapper<PlayerInputLock> MAPPER = ComponentMapper.getFor(PlayerInputLock.class);
}
