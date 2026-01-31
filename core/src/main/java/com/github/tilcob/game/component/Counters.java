package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.ObjectIntMap;

public class Counters implements Component {
    public static final ComponentMapper<Counters> MAPPER = ComponentMapper.getFor(Counters.class);

    private final ObjectIntMap<String> counters = new ObjectIntMap<>();

    public int get(String key) {
        return counters.get(key, 0);
    }

    public void set(String key, int value) {
        counters.put(key, value);
    }

    public int increment(String key, int amount) {
        int next = get(key) + amount;
        counters.put(key, next);
        return next;
    }

    public ObjectIntMap<String> getCounters() {
        return counters;
    }
}
