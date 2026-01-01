package com.github.tilcob.game.ui.model;

import com.badlogic.gdx.utils.Disposable;
import com.github.tilcob.game.GdxGame;
import com.github.tilcob.game.event.GameEventBus;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class ViewModel {
    protected final GdxGame game;
    protected final PropertyChangeSupport propertyChangeSupport;
    protected final GameEventBus gameEventBus;

    public ViewModel(GdxGame game, GameEventBus eventBus) {
        this.game = game;
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.gameEventBus = eventBus;
    }

    public <T> void onPropertyChange(String propertyName, Class<T> propertyType, OnPropertyChange<T> consumer) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, event -> {
            consumer.onChange(propertyType.cast(event.getNewValue()));
        });
    }

    public void clearPropertyChanges() {
        for (PropertyChangeListener listener : propertyChangeSupport.getPropertyChangeListeners()) {
            propertyChangeSupport.removePropertyChangeListener(listener);
        }
    }

    public abstract void dispose();

    @FunctionalInterface
    public interface OnPropertyChange<T> {
        void onChange(T value);
    }
}
