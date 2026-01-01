package com.github.tilcob.game.event;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.utils.Array;

import java.util.function.Consumer;

public class GameEventBus {
    private final Array<Consumer<Event>> listeners = new Array<>();

    public void subscribe(Consumer<Event> listener) {
        listeners.add(listener);
    }

    public void fire(Event event) {
        listeners.forEach(listener -> listener.accept(event));
    }
}
