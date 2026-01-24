package com.github.tilcob.game.ui.model;

import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.AutosaveEvent;
import com.github.tilcob.game.event.PauseEvent;
import com.github.tilcob.game.event.UiEvent;
import com.github.tilcob.game.screen.MenuScreen;
import com.github.tilcob.game.screen.ScreenNavigator;

public class PauseViewModel extends ViewModel {
    private final ScreenNavigator screenNavigator;

    public PauseViewModel(GameServices services, ScreenNavigator screenNavigator) {
        super(services);
        this.screenNavigator = screenNavigator;

        getEventBus().subscribe(UiEvent.class, this::onUiEvent);
    }

    private void onUiEvent(UiEvent event) {
        if (event.isHandled()) return;
        if (event.action() == UiEvent.Action.RELEASE) return;
        event.handle();
        switch (event.command()) {
            case UP -> onUp();
            case DOWN -> onDown();
            case ATTACK -> onSelect();
            case PAUSE, CANCEL -> resumeGame();
            default -> {}
        }
    }

    private void onSelect() {
        propertyChangeSupport.firePropertyChange(Constants.ON_SELECT, null, true);
    }

    private void onDown() {
        propertyChangeSupport.firePropertyChange(Constants.ON_DOWN, null, true);
    }

    private void onUp() {
        propertyChangeSupport.firePropertyChange(Constants.ON_UP, null, true);
    }

    public void resumeGame() {
        getEventBus().fire(new PauseEvent(PauseEvent.Action.RESUME));
    }

    public void quitToMenu() {
        getEventBus().fire(new AutosaveEvent(AutosaveEvent.AutosaveReason.MAP_CHANGE));
        screenNavigator.setScreen(MenuScreen.class);
    }

    @Override
    public void dispose() {
        getEventBus().unsubscribe(UiEvent.class, this::onUiEvent);
    }
}
