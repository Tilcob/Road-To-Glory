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
    }

    public void startGame() {
        screenNavigator.setScreen(GameScreen.class);
    }

    public void quitGame() {
        Gdx.app.exit();
    }
}
