package com.github.tilcob.game;

import com.badlogic.gdx.Game;
import com.github.tilcob.game.screen.FirstScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class GdxGame extends Game {
    @Override
    public void create() {
        setScreen(new FirstScreen());
    }
}
