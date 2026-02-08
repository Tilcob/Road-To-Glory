package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class InteractIndicatorSuppression implements Component {
    public static final ComponentMapper<InteractIndicatorSuppression> MAPPER =
        ComponentMapper.getFor(InteractIndicatorSuppression.class);
}
