package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class DeathHandled implements Component {
    public static final ComponentMapper<DeathHandled> MAPPER = ComponentMapper.getFor(DeathHandled.class);
}
