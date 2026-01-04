package com.github.tilcob.game.event;

import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GameEventBus {
    private final Map<Class<?>, Array<Consumer<?>>> listeners = new HashMap<>();

    public <T> void subscribe(Class<T> type, Consumer<T> listener) {
        listeners.computeIfAbsent(type, k -> new Array<>()).add(listener);
    }

    public <T> void unsubscribe(Class<T> type, Consumer<T> listener) {
        Array<Consumer<?>> list = listeners.get(type);
        if (list == null) return;

        list.removeValue(listener, true);

        if (list.isEmpty()) listeners.remove(type);
    }

    public <T> void fire(T event) {
        Class<?> type = event.getClass();
        Array<Consumer<?>> list = listeners.get(type);
        if (list == null) return;

        for (Consumer<?> listener : list) {
            ((Consumer<T>) listener).accept(event);
        }
    }
}
