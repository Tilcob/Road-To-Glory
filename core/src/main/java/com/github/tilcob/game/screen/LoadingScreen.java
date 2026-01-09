package com.github.tilcob.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.github.tilcob.game.GdxGame;
import com.github.tilcob.game.assets.*;
import com.github.tilcob.game.quest.QuestFactory;

public class LoadingScreen extends ScreenAdapter {
    private final GdxGame game;
    private final AssetManager assetManager;
    private final QuestFactory questFactory;

    public LoadingScreen(GdxGame game, AssetManager assetManager) {
        this.game = game;
        this.assetManager = assetManager;
        this.questFactory = new QuestFactory(game.getEventBus());
    }

    @Override
    public void show() {
        for (AtlasAsset asset: AtlasAsset.values()) {
            assetManager.queue(asset);
        }
        for (SoundAsset soundAsset : SoundAsset.values()) {
            assetManager.queue(soundAsset);
        }
        for (QuestAsset questAsset : QuestAsset.values()) {
            game.getAllQuests().putAll(questFactory.loadAll(questAsset));
        }
        assetManager.queue(SkinAsset.DEFAULT);
    }

    @Override
    public void render(float delta) {
        if (assetManager.update()) {
            Gdx.app.debug("LoadingScreen", "Finished asset loading.");
            creatScreens();
            game.removeScreen(this);
            dispose();
            game.setScreen(MenuScreen.class);
        }
    }

    private void creatScreens() {
        game.addScreen(new MenuScreen(game));
        game.addScreen(new GameScreen(game));
    }
}
