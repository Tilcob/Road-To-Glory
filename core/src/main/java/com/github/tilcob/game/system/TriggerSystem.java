package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.component.*;
import com.github.tilcob.game.trigger.ChangeMapTriggerHandler;
import com.github.tilcob.game.trigger.ChestTriggerHandler;
import com.github.tilcob.game.trigger.TrapTriggerHandler;
import com.github.tilcob.game.trigger.TriggerHandler;
import java.util.EnumMap;

public class TriggerSystem extends IteratingSystem {
    private final EnumMap<Trigger.Type, TriggerHandler> handlers;

    public TriggerSystem(AudioManager audioManager) {
        super(Family.all(Trigger.class).get());
        this.handlers = new EnumMap<>(Trigger.Type.class);

        handlers.put(Trigger.Type.TRAP, new TrapTriggerHandler(audioManager));
        handlers.put(Trigger.Type.CHEST, new ChestTriggerHandler(audioManager));
        handlers.put(Trigger.Type.CHANGE_MAP, new ChangeMapTriggerHandler());
    }

    @Override
    protected void processEntity(Entity triggerEntity, float deltaTime) {
        Trigger trigger = Trigger.MAPPER.get(triggerEntity);
        if (trigger.getEntities().isEmpty()) return;

        TriggerHandler handler = handlers.get(trigger.getType());
        if (handler == null) return;

        for (Entity triggeringEntity : trigger.getEntities()) {
            Tiled tiled = Tiled.MAPPER.get(triggerEntity);
            Entity entity = entityByTiledId(tiled.getId()) != null ? entityByTiledId(tiled.getId()) : triggerEntity;
            handler.execute(entity, triggeringEntity);
        }

        trigger.getEntities().clear();
    }

    private Entity entityByTiledId(int id) {
        if (id == 0)  return null;
        ImmutableArray<Entity> entities = getEngine().getEntitiesFor(Family.all(Tiled.class).get());
        for (Entity entity : entities) {
            if (Tiled.MAPPER.get(entity).getId() == id) {
                return entity;
            }
        }
        return null;
    }
}
