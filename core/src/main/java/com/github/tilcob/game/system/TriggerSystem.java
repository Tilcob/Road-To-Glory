package com.github.tilcob.game.system;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntMap;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.component.Tiled;
import com.github.tilcob.game.component.Trigger;
import com.github.tilcob.game.event.ExitTriggerEvent;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.trigger.*;

import java.util.EnumMap;

public class TriggerSystem extends IteratingSystem implements Disposable {
    private final EnumMap<Trigger.Type, TriggerHandler> handlers;
    private final GameEventBus eventBus;
    private final IntMap<Entity> entitiesByTiledId = new IntMap<>();
    private final EntityListener tiledListener = new EntityListener() {
        @Override
        public void entityAdded(Entity entity) {
            addEntityToIndex(entity);
        }

        @Override
        public void entityRemoved(Entity entity) {
            removeEntityFromIndex(entity);
        }
    };

    public TriggerSystem(AudioManager audioManager, GameEventBus eventBus) {
        super(Family.all(Trigger.class).get());
        this.handlers = new EnumMap<>(Trigger.Type.class);
        this.eventBus = eventBus;
        eventBus.subscribe(ExitTriggerEvent.class, this::onExit);

        handlers.put(Trigger.Type.TRAP, new TrapTriggerHandler(audioManager));
        handlers.put(Trigger.Type.CHEST, new ChestTriggerHandler(eventBus));
        handlers.put(Trigger.Type.CHANGE_MAP, new ChangeMapTriggerHandler());
        handlers.put(Trigger.Type.QUEST, new QuestTrigger(eventBus));
        handlers.put(Trigger.Type.DIALOG, new DialogTrigger());
        handlers.put(Trigger.Type.CUTSCENE, new CutsceneTrigger(eventBus));
    }

    @Override
    protected void processEntity(Entity triggerEntity, float deltaTime) {
        Trigger trigger = Trigger.MAPPER.get(triggerEntity);
        if (trigger.getEntities().isEmpty()) return;

        TriggerHandler handler = handlers.get(trigger.getType());
        if (handler == null) return;

        for (Entity triggeringEntity : trigger.getEntities()) {
            Entity entity = resolveTriggerEntity(triggerEntity);
            handler.onEnter(entity, triggeringEntity);
        }

        trigger.getEntities().clear();
    }

    private void onExit(ExitTriggerEvent event) {
        Trigger trigger = Trigger.MAPPER.get(event.trigger());
        TriggerHandler handler = handlers.get(trigger.getType());
        if (handler == null) return;
        Entity entity = resolveTriggerEntity(event.trigger());
        handler.onExit(entity, event.player());
    }

    @Override
    public void addedToEngine(Engine engine) {
        super.addedToEngine(engine);
        ImmutableArray<Entity> entities = engine.getEntitiesFor(Family.all(Tiled.class).get());
        for (Entity entity : entities) {
            addEntityToIndex(entity);
        }
        engine.addEntityListener(Family.all(Tiled.class).get(), tiledListener);
    }

    @Override
    public void removedFromEngine(Engine engine) {
        engine.removeEntityListener(tiledListener);
        entitiesByTiledId.clear();
        super.removedFromEngine(engine);
    }

    private Entity resolveTriggerEntity(Entity triggerEntity) {
        Tiled tiled = Tiled.MAPPER.get(triggerEntity);
        if (tiled == null) return triggerEntity;
        Entity entity = entityByTiledId(tiled.getId());
        return entity != null ? entity : triggerEntity;
    }

    private void addEntityToIndex(Entity entity) {
        if (Trigger.MAPPER.get(entity) != null) return;
        Tiled tiled = Tiled.MAPPER.get(entity);
        if (tiled == null) return;
        int id = tiled.getId();
        if (id == 0) return;
        entitiesByTiledId.put(id, entity);
    }

    private void removeEntityFromIndex(Entity entity) {
        Tiled tiled = Tiled.MAPPER.get(entity);
        if (tiled == null) return;
        int id = tiled.getId();
        if (id == 0) return;
        if (entitiesByTiledId.get(id) == entity) {
            entitiesByTiledId.remove(id);
        }
    }

    private Entity entityByTiledId(int id) {
        if (id == 0) return null;
        return entitiesByTiledId.get(id);
    }

    @Override
    public void dispose() {
        eventBus.unsubscribe(ExitTriggerEvent.class, this::onExit);
        Engine engine = getEngine();
        if (engine != null) {
            engine.removeEntityListener(tiledListener);
        }
        entitiesByTiledId.clear();
    }
}
