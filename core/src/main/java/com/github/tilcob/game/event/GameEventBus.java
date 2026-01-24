package com.github.tilcob.game.event;

import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GameEventBus {
    private final Map<Class<?>, Array<Consumer<?>>> listeners = new HashMap<>();

    public <T> void subscribe(Class<T> type, Consumer<? super T> listener) {
        listeners.computeIfAbsent(type, k -> new Array<>()).add(listener);
    }

    public <T> void unsubscribe(Class<T> type, Consumer<? super T> listener) {
        Array<Consumer<?>> list = listeners.get(type);
        if (list == null) return;

        list.removeValue(listener, true);

        if (list.isEmpty()) listeners.remove(type);
    }

    public <T> void fire(T event) {
        if (event == null) return;
        Array<Consumer<?>> list = listeners.get(event.getClass());
        if (list == null) return;

        Array<Consumer<?>> snapshot = new Array<>(list);
        for (Consumer<?> listener : snapshot) {
            ((Consumer<? super T>) listener).accept(event);
            if (event instanceof GameEvent gameEvent && gameEvent.isHandled()) return;
        }
    }
}
