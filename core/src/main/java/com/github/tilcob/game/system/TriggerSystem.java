package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.component.Tiled;
import com.github.tilcob.game.component.Trigger;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.trigger.*;

import java.util.EnumMap;

public class TriggerSystem extends IteratingSystem {
    private final EnumMap<Trigger.Type, TriggerHandler> handlers;

    public TriggerSystem(AudioManager audioManager, GameEventBus eventBus) {
        super(Family.all(Trigger.class).get());
        this.handlers = new EnumMap<>(Trigger.Type.class);

        handlers.put(Trigger.Type.TRAP, new TrapTriggerHandler(audioManager));
        handlers.put(Trigger.Type.CHEST, new ChestTriggerHandler());
        handlers.put(Trigger.Type.CHANGE_MAP, new ChangeMapTriggerHandler());
        handlers.put(Trigger.Type.QUEST, new QuestTrigger(eventBus));
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
