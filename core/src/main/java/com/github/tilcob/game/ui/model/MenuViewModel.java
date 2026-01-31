package com.github.tilcob.game.ui.model;

import com.badlogic.gdx.Gdx;
import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.screen.GameScreen;
import com.github.tilcob.game.screen.ScreenNavigator;

public class MenuViewModel extends ViewModel {
    private final ScreenNavigator screenNavigator;

    public MenuViewModel(GameServices services, ScreenNavigator screenNavigator) {
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
