package com.github.tilcob.game.trigger;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.maps.MapObject;
import com.github.tilcob.game.assets.MapAsset;
import com.github.tilcob.game.component.MapChange;
import com.github.tilcob.game.component.Tiled;
import com.github.tilcob.game.config.Constants;

public class ChangeMapTriggerHandler implements TriggerHandler {

    @Override
    public void execute(Entity trigger, Entity triggeringEntity) {
        MapObject mapObjectRef = Tiled.MAPPER.get(trigger).getMapObjectRef();
        String mapStr = mapObjectRef.getProperties().get(Constants.TO_MAP, "", String.class);
        MapAsset mapAsset = MapAsset.valueOf(mapStr);

        triggeringEntity.add(new MapChange(mapAsset));
    }

    @Override
    public void exit(Entity trigger, Entity triggeringEntity) {

    }
}
