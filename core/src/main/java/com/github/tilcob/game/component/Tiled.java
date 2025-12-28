package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.maps.MapObject;
import com.github.tilcob.game.config.Constants;

public class Tiled implements Component {
    public static final ComponentMapper<Tiled> MAPPER = ComponentMapper.getFor(Tiled.class);

    private final int id;
    private final MapObject mapObjectRef;

    public Tiled(MapObject mapObjectRef) {
        this.id = mapObjectRef.getProperties().get(Constants.ID, -1, Integer.class);
        this.mapObjectRef = mapObjectRef;
    }

    public int getId() {
        return id;
    }

    public MapObject getMapObjectRef() {
        return mapObjectRef;
    }
}
