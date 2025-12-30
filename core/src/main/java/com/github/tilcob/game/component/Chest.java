package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.Gdx;

public class Chest implements Component {
    public static final ComponentMapper<Chest> MAPPER = ComponentMapper.getFor(Chest.class);

    public void open() {
        Gdx.app.log("Chest", "Opening");
    }
}
