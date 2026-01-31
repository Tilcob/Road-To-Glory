package com.github.tilcob.game.ui.model;

import com.badlogic.gdx.Gdx;
import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.UiEvent;
import com.github.tilcob.game.screen.GameScreen;
import com.github.tilcob.game.screen.ScreenNavigator;
import com.github.tilcob.game.ui.UiServices;

public class MenuViewModel extends ViewModel {
    private final ScreenNavigator screenNavigator;

    public MenuViewModel(GameServices services, ScreenNavigator screenNavigator, UiServices uiServices) {
        super(services);
        this.screenNavigator = screenNavigator;

        getEventBus().subscribe(UiEvent.class, this::onUiEvent);
    }

    private void onUiEvent(UiEvent uiEvent) {
        if (uiEvent.action() == UiEvent.Action.RELEASE) return;

        switch (uiEvent.command()) {
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
