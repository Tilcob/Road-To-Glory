package com.github.tilcob.game.ui.model;

import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.event.UiEvent;
import com.github.tilcob.game.ui.UiServices;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class ViewModel {
    protected final GameServices services;
    protected final PropertyChangeSupport propertyChangeSupport;
    protected final GameEventBus gameEventBus;
    private boolean active = true;

    public ViewModel(GameServices services) {
        this.services = services;
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.gameEventBus = services.getEventBus();

        gameEventBus.subscribe(UiEvent.class, this::onUiEvent);
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

    protected void onUiEvent(UiEvent event) {
        if (!active) return;
        if (event.action() == UiEvent.Action.RELEASE) return;

        switch (event.command()) {
            case LEFT -> onLeft();
            case RIGHT -> onRight();
            case UP -> onUp();
            case DOWN -> onDown();
            case SELECT -> onSelect();
            case CANCEL -> onCancel();
            default -> {
            }
        }
    }

    protected void onSelect() {
        this.propertyChangeSupport.firePropertyChange(Constants.ON_SELECT, null, true);
    }

    protected void onDown() {
        this.propertyChangeSupport.firePropertyChange(Constants.ON_DOWN, null, true);
    }

    protected void onUp() {
        this.propertyChangeSupport.firePropertyChange(Constants.ON_UP, null, true);
    }

    protected void onRight() {
        this.propertyChangeSupport.firePropertyChange(Constants.ON_RIGHT, null, true);
    }

    protected void onLeft() {
        this.propertyChangeSupport.firePropertyChange(Constants.ON_LEFT, null, true);
    }

    protected void onCancel() {
        this.propertyChangeSupport.firePropertyChange(Constants.ON_CANCEL, null, true);
    }

    protected boolean setOpen(boolean open, boolean currentOpen, String propertyName) {
        if (currentOpen == open) {
            return currentOpen;
        }
        setActive(open);
        propertyChangeSupport.firePropertyChange(propertyName, currentOpen, open);
        return open;
    }

    public GameEventBus getEventBus() {
        return gameEventBus;
    }

    public UiServices getUiServices() {
        return services.getUiServices();
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public void dispose() {
        gameEventBus.unsubscribe(UiEvent.class, this::onUiEvent);
    }

    @FunctionalInterface
    public interface OnPropertyChange<T> {
        void onChange(T value);
    }
}
