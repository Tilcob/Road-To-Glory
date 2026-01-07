package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.github.tilcob.game.component.MapChange;
import com.github.tilcob.game.component.Physic;
import com.github.tilcob.game.component.Transform;
import com.github.tilcob.game.event.AutosaveEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.save.states.StateManager;
import com.github.tilcob.game.tiled.TiledManager;

public class MapChangeSystem extends IteratingSystem {
    private final TiledManager tiledManager;
    private final GameEventBus eventBus;
    private final StateManager stateManager;

    public MapChangeSystem(TiledManager tiledManager, GameEventBus eventBus, StateManager stateManager) {
        super(Family.all(MapChange.class).get());
        this.tiledManager = tiledManager;
        this.eventBus = eventBus;
        this.stateManager = stateManager;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        MapChange mapChange = MapChange.MAPPER.get(entity);
        stateManager.saveMap(mapChange.getMapAsset());
        tiledManager.setMap(tiledManager.loadMap(mapChange.getMapAsset()));
        Vector2 spawn = tiledManager.getSpawnPoint();
        eventBus.fire(new AutosaveEvent(AutosaveEvent.AutosaveReason.MAP_CHANGE));

        Transform.MAPPER.get(entity).getPosition().set(spawn.x, spawn.y);
        Physic.MAPPER.get(entity).getBody().setTransform(spawn.x, spawn.y, 0);
        entity.remove(MapChange.class);
    }
}
