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
    private final QuestFactory questFactory;
    private final DialogLoader dialogLoader;
    private final Consumer<LoadingScreen> onFinished;

    public LoadingScreen(GameServices services, Consumer<LoadingScreen> onFinished) {
        this.services = services;
        this.questFactory = new QuestFactory(services.getEventBus());
        this.dialogLoader = new DialogLoader();
        this.onFinished = onFinished;
    }

    @Override
    public void show() {
        for (AtlasAsset asset: AtlasAsset.values()) {
            services.getAssetManager().queue(asset);
        }
        for (SoundAsset soundAsset : SoundAsset.values()) {
            services.getAssetManager().queue(soundAsset);
        }
        for (QuestAsset questAsset : QuestAsset.values()) {
            services.getAllQuests().putAll(questFactory.loadAll(questAsset));
        }
        for (DialogAsset dialogAsset : DialogAsset.values()) {
            services.getAllDialogs().put(dialogAsset.name().toLowerCase(), dialogLoader.load(dialogAsset));
        }
        services.getAssetManager().queue(SkinAsset.DEFAULT);
    }

    @Override
    public void render(float delta) {
        if (services.getAssetManager().update()) {
            Gdx.app.debug("LoadingScreen", "Finished asset loading.");
            onFinished.accept(this);
        }
    }
}
