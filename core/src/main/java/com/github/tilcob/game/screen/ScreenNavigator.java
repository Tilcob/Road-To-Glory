package com.github.tilcob.game.screen;

import com.badlogic.gdx.Screen;

public interface ScreenNavigator {
    void setScreen(Class<? extends Screen> screenClass);
}
