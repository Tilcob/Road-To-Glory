package com.github.tilcob.game.screen;

import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.assets.*;
import com.github.tilcob.game.dialog.DialogLoader;
import com.github.tilcob.game.dialog.YarnDialogLoader;
import com.github.tilcob.game.quest.QuestFactory;

public class GameLoader {
    private final GameServices services;
    private final QuestFactory questFactory;
    private final YarnDialogLoader dialogLoader;

    public GameLoader(GameServices services) {
        this.services = services;
        this.questFactory = new QuestFactory(services.getEventBus());
        this.dialogLoader = new YarnDialogLoader();
    }

    public void queueAll() {
        for (AtlasAsset asset : AtlasAsset.values()) {
            services.getAssetManager().queue(asset);
        }
        for (SoundAsset soundAsset : SoundAsset.values()) {
            services.getAssetManager().queue(soundAsset);
        }
        for (QuestAsset questAsset : QuestAsset.values()) {
            services.getAllQuests().putAll(questFactory.loadAll(questAsset));
        }
        for (DialogAsset dialogAsset : DialogAsset.values()) {
            services.getAllDialogs().put(dialogAsset.getNpcName(),
                dialogLoader.load(dialogAsset.getFileHandle()));
        }
        services.getAssetManager().queue(SkinAsset.DEFAULT);
    }

    public boolean update() {
        return services.getAssetManager().update();
    }
}
