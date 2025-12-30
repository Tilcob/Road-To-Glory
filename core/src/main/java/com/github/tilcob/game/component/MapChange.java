package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.github.tilcob.game.assets.MapAsset;

public class MapChange implements Component {
    public static final ComponentMapper<MapChange> MAPPER = ComponentMapper.getFor(MapChange.class);

    private final MapAsset mapAsset;

    public MapChange(MapAsset mapAsset) {
        this.mapAsset = mapAsset;
    }

    public MapAsset getMapAsset() {
        return mapAsset;
    }
}
