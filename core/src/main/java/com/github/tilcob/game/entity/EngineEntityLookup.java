package com.github.tilcob.game.entity;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.github.tilcob.game.component.Npc;

public class EngineEntityLookup implements EntityLookup {
    private final Engine engine;
    private final EntityIdService service;

    public EngineEntityLookup(Engine engine, EntityIdService service) {
        this.engine = engine;
        this.service = service;
    }

    @Override
    public Entity find(String name) {
        Entity entity = service.findNpc(name);
        if (entity != null) return entity;

        for (Entity e : engine.getEntitiesFor(Family.all(Npc.class).get())) {
            Npc npc = Npc.MAPPER.get(e);
            if (npc != null && npc.getName().equalsIgnoreCase(name)) {
                return e;
            }
        }

        return null;
    }
}
