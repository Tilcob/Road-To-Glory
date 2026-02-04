package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class Occluder implements Component {
    public static final ComponentMapper<Occluder> MAPPER = ComponentMapper.getFor(Occluder.class);
}
