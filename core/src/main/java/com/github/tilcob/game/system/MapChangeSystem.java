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
import com.github.tilcob.game.event.MapChangeEvent;
import com.github.tilcob.game.save.states.StateManager;
import com.github.tilcob.game.tiled.TiledManager;

import java.util.Locale;

public class MapChangeSystem extends IteratingSystem {
    private final TiledManager tiledManager;
    private final GameEventBus eventBus;
    private final StateManager stateManager;

    public MapChangeSystem(TiledManager tiledManager, GameEventBus eventBus, StateManager stateManager) {
        super(Family.all(MapChange.class, Transform.class, Physic.class).get());
        this.tiledManager = tiledManager;
        this.eventBus = eventBus;
        this.stateManager = stateManager;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        MapChange mapChange = MapChange.MAPPER.get(entity);
        Transform transform = Transform.MAPPER.get(entity);
        Physic physic = Physic.MAPPER.get(entity);
        stateManager.saveMap(mapChange.getMapAsset());
        tiledManager.setMap(tiledManager.loadMap(mapChange.getMapAsset()));
        Vector2 spawn = tiledManager.getSpawnPoint();
        eventBus.fire(new AutosaveEvent(AutosaveEvent.AutosaveReason.MAP_CHANGE));
        eventBus.fire(new MapChangeEvent(mapChange.getMapAsset().name().toLowerCase(Locale.ROOT)));

        float spawnX = transform.getPosition().x;
        float spawnY = transform.getPosition().y;
        if (spawn != null) {
            spawnX = spawn.x;
            spawnY = spawn.y;
        }
        transform.getPosition().set(spawnX, spawnY);
        physic.getBody().setTransform(spawnX, spawnY, 0);
        entity.remove(MapChange.class);
    }
}
