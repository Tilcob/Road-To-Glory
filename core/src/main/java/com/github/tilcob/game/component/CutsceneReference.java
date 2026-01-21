package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

public class CutsceneReference implements Component {
    public static final ComponentMapper<CutsceneReference> MAPPER = ComponentMapper.getFor(CutsceneReference.class);

    private final String cutsceneId;

    public CutsceneReference(String cutsceneId) {
        this.cutsceneId = cutsceneId;
    }

    public String getCutsceneId() {
        return cutsceneId;
    }
}
