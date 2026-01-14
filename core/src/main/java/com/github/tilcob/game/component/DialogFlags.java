package com.github.tilcob.game.component;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.utils.ObjectMap;

public class DialogFlags implements Component {
    public static final ComponentMapper<DialogFlags> MAPPER = ComponentMapper.getFor(DialogFlags.class);

    private final ObjectMap<String, Boolean> flags = new ObjectMap<>();

    public ObjectMap<String, Boolean> getFlags() {
        return flags;
    }

    public boolean get(String key) {
        return flags.get(key, false);
    }

    public void set(String key, boolean value) {
        flags.put(key, value);
    }
}
