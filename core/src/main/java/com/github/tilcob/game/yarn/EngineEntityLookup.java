package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.github.tilcob.game.component.Npc;

public class EngineEntityLookup implements EntityLookup {
    private final Engine engine;

    public EngineEntityLookup(Engine engine) {
        this.engine = engine;
    }

    @Override
    public Entity find(Entity player, String entityId) {
        if (entityId == null || entityId.isBlank()) {
            return player;
        }
        String normalized = entityId.trim();
        if ("player".equalsIgnoreCase(normalized) || "self".equalsIgnoreCase(normalized)) {
            return player;
        }
        if (engine == null) {
            return null;
        }
        ImmutableArray<Entity> npcs = engine.getEntitiesFor(Family.all(Npc.class).get());
        for (Entity entity : npcs) {
            Npc npc = Npc.MAPPER.get(entity);
            if (npc != null && normalized.equalsIgnoreCase(npc.getName())) {
                return entity;
            }
        }
        return null;
    }
}
