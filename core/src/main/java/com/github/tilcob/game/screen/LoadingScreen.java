package com.github.tilcob.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.GdxGame;
import com.github.tilcob.game.assets.*;
import com.github.tilcob.game.dialog.DialogLoader;
import com.github.tilcob.game.quest.QuestFactory;

import java.util.function.Consumer;

public class LoadingScreen extends ScreenAdapter {
    private final GameServices services;
    private final GameLoader loader;
    private final Consumer<LoadingScreen> onFinished;

    public LoadingScreen(GameServices services, Consumer<LoadingScreen> onFinished) {
        this.services = services;
        this.loader = new GameLoader(services);
        this.onFinished = onFinished;
    }

    @Override
    public void show() {
        loader.queueAll();
    }

    @Override
    public void render(float delta) {
        if (loader.update()) {
            Gdx.app.debug("LoadingScreen", "Finished asset loading.");
            onFinished.accept(this);
        }
    }
}
