package com.github.tilcob.game.ui.model;

import com.badlogic.gdx.Gdx;
import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.UiEvent;
import com.github.tilcob.game.screen.GameScreen;
import com.github.tilcob.game.screen.ScreenNavigator;

public class MenuViewModel extends ViewModel {
    private final AudioManager audioManager;
    private final ScreenNavigator screenNavigator;

    public MenuViewModel(GameServices services, ScreenNavigator screenNavigator) {
        super(services);
        this.audioManager = services.getAudioManager();
        this.screenNavigator = screenNavigator;

        getEventBus().subscribe(UiEvent.class, this::onUiEvent);
    }

    private void onUiEvent(UiEvent uiEvent) {
        switch (uiEvent.command()) {
            case LEFT ->  onLeft();
            case RIGHT ->  onRight();
            case UP -> onUp();
            case DOWN -> onDown();
            case SELECT -> onSelect();
        }
    }

    private void onSelect() {
        this.propertyChangeSupport.firePropertyChange(Constants.ON_SELECT, null, null);
    }

    private void onDown() {
        this.propertyChangeSupport.firePropertyChange(Constants.ON_DOWN, null, null);
    }

    private void onUp() {
        this.propertyChangeSupport.firePropertyChange(Constants.ON_UP, null, null);
    }

    private void onRight() {
        this.propertyChangeSupport.firePropertyChange(Constants.ON_RIGHT, null, null);
    }

    private void onLeft() {
        this.propertyChangeSupport.firePropertyChange(Constants.ON_LEFT, null, null);
    }


    public float getMusicVolume() {
        return audioManager.getMusicVolume();
    }

    public float getSoundVolume() {
        return audioManager.getSoundVolume();
    }

    public void setMusicVolume(float volume) {
        audioManager.setMusicVolume(volume);
    }

    public void setSoundVolume(float volume) {
        audioManager.setSoundVolume(volume);
    }

    public void startGame() {
        screenNavigator.setScreen(GameScreen.class);
    }

    public void quitGame() {
        Gdx.app.exit();
    }

    @Override
    public void dispose() {
        getEventBus().unsubscribe(UiEvent.class, this::onUiEvent);
    }
}
