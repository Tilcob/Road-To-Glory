package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.component.MapChange;
import com.github.tilcob.game.component.Physic;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.tiled.TiledManager;

public class MapChangeSystem extends IteratingSystem {
    private final TiledManager tiledManager;

    public MapChangeSystem(TiledManager tiledManager) {
        super(Family.all(MapChange.class).get());
        this.tiledManager = tiledManager;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        MapChange mapChange = MapChange.MAPPER.get(entity);
        tiledManager.setMap(tiledManager.loadMap(mapChange.getMapAsset()));
        Vector2 spawn = tiledManager.getSpawnPoint();

        Transform.MAPPER.get(entity).getPosition().set(spawn.x, spawn.y);
        Physic.MAPPER.get(entity).getBody().setTransform(spawn.x, spawn.y, 0);
        entity.remove(MapChange.class);
    }
}
