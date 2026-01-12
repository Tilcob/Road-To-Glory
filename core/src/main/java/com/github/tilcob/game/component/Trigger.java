package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.ObjectSet;

public class Trigger implements Component {
    public static final ComponentMapper<Trigger> MAPPER = ComponentMapper.getFor(Trigger.class);

    private final ObjectSet<Entity> entities = new ObjectSet<>();
    private final Type type;


    public Trigger(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public ObjectSet<Entity> getEntities() {
        return entities;
    }

    public void add(Entity entity) {
        entities.add(entity);
    }

    public void remove(Entity entity) {
        entities.remove(entity);
    }

    public enum Type {
        TRAP,
        CHEST,
        CHANGE_MAP,
        QUEST,
        DIALOG,
    }
}
