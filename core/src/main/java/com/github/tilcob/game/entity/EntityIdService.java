package com.github.tilcob.game.entity;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.tilcob.game.component.Npc;

public class EntityIdService {
    private final ObjectMap<String, Entity> npcByName = new ObjectMap<>();

    public EntityIdService(Engine engine) {
        for (Entity e : engine.getEntitiesFor(Family.all(Npc.class).get())) {
            index(e);
        }

        engine.addEntityListener(Family.all(Npc.class).get(), new EntityListener() {
            @Override
            public void entityAdded(Entity entity) {
                index(entity);
            }

            @Override
            public void entityRemoved(Entity entity) {
                remove(entity);
            }
        });
    }

    private void index(Entity entity) {
        Npc npc = Npc.MAPPER.get(entity);
        if (npc == null || npc.getName() == null) return;

        npcByName.put(npc.getName().toLowerCase(), entity);
    }

    private void remove(Entity entity) {
        Npc npc = Npc.MAPPER.get(entity);
        if (npc == null || npc.getName() == null) return;

        npcByName.remove(npc.getName().toLowerCase());
    }

    public Entity findNpc(String name) {
        if (name == null) return null;
        return npcByName.get(name.toLowerCase());
    }
}
