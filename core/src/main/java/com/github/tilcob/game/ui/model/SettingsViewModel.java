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
        this.open = setOpen(value, this.open, Constants.OPEN_SETTINGS);
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
