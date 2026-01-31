package com.github.tilcob.game.ui.model;

import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.UiEvent;

public class SettingsViewModel extends ViewModel {

    public SettingsViewModel(GameServices services) {
        super(services);

        gameEventBus.subscribe(UiEvent.class, this::onUiEvent);
    }

    private void onUiEvent(UiEvent event) {
        if (event.action() == UiEvent.Action.RELEASE) return;

        switch (event.command()) {
            case LEFT ->  onLeft();
            case RIGHT ->  onRight();
            case UP -> onUp();
            case DOWN -> onDown();
            case SELECT -> onSelect();
        }
    }

    private void onSelect() {
        this.propertyChangeSupport.firePropertyChange(Constants.ON_SELECT, null, true);
    }

    private void onDown() {
        this.propertyChangeSupport.firePropertyChange(Constants.ON_DOWN, null, true);
    }

    private void onUp() {
        this.propertyChangeSupport.firePropertyChange(Constants.ON_UP, null, true);
    }

    private void onRight() {
        this.propertyChangeSupport.firePropertyChange(Constants.ON_RIGHT, null, true);
    }

    private void onLeft() {
        this.propertyChangeSupport.firePropertyChange(Constants.ON_LEFT, null, true);
    }


}
