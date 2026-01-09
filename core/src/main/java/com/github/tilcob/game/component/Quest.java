package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.maps.MapObject;
import com.github.tilcob.game.config.Constants;

public class Quest implements Component {
    public static final ComponentMapper<Quest> MAPPER = ComponentMapper.getFor(Quest.class);
    private final String questId;

    public Quest(String questId) {
        this.questId = questId;
    }

    public String getQuestId() {
        return questId;
    }
}
