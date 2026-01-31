package com.github.tilcob.game.ui.model;

import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.UiOverlayEvent;

public class SettingsViewModel extends ViewModel {
    private boolean open = false;

    public SettingsViewModel(GameServices services) {
        super(services);

        getEventBus().subscribe(UiOverlayEvent.class, this::onOverlayEvent);
    }

    private void onOverlayEvent(UiOverlayEvent event) {
        if (event == null) return;

        switch (event.type()) {
            case OPEN_SETTINGS -> setOpen(true);
            case CLOSE_SETTINGS -> setOpen(false);
            case TOGGLE_SETTINGS -> setOpen(!open);
        }
    }

    private void setOpen(boolean value) {
        if (this.open == value) return;

        boolean old = this.open;
        this.open = value;
        setActive(value);
        propertyChangeSupport.firePropertyChange(Constants.OPEN_SETTINGS, old, value);
    }

    public float getMusicVolume() {
        return getUiServices().getMusicVolume();
    }

    public float getSoundVolume() {
        return getUiServices().getSoundVolume();
    }

    public void setMusicVolume(float volume) {
        getUiServices().setMusicVolume(volume);
    }

    public void setSoundVolume(float volume) {
        getUiServices().setSoundVolume(volume);
    }

    public void close() {
        gameEventBus.fire(new UiOverlayEvent(UiOverlayEvent.Type.CLOSE_SETTINGS));
    }

    public boolean isOpen() {
        return open;
    }
}
