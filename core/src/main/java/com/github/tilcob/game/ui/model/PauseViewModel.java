package com.github.tilcob.game.ui.model;

import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.event.AutosaveEvent;
import com.github.tilcob.game.event.PauseEvent;
import com.github.tilcob.game.event.UiEvent;
import com.github.tilcob.game.screen.MenuScreen;
import com.github.tilcob.game.screen.ScreenNavigator;
import com.github.tilcob.game.ui.UiServices;

public class PauseViewModel extends ViewModel {
    private final ScreenNavigator screenNavigator;

    public PauseViewModel(GameServices services, ScreenNavigator screenNavigator) {
        super(services);
        this.screenNavigator = screenNavigator;
    }

    public void resumeGame() {
        getEventBus().fire(new PauseEvent(PauseEvent.Action.RESUME));
    }

    public void quitToMenu() {
        getEventBus().fire(new AutosaveEvent(AutosaveEvent.AutosaveReason.MAP_CHANGE));
        screenNavigator.setScreen(MenuScreen.class);
    }
}
