package com.github.tilcob.game;

import com.github.tilcob.game.assets.AtlasAsset;
import com.github.tilcob.game.assets.SkinAsset;
import com.github.tilcob.game.assets.SoundAsset;
import com.github.tilcob.game.dialog.YarnDialogLoader;
import com.github.tilcob.game.quest.QuestFactory;
import com.github.tilcob.game.quest.QuestJson;

public class GameLoader {
    private final GameServices services;
    private final QuestFactory questFactory;
    private final YarnDialogLoader dialogLoader;

    public GameLoader(GameServices services) {
        this.services = services;
        this.questFactory = new QuestFactory(services.getEventBus(), services.getQuestRepository());
        this.dialogLoader = new YarnDialogLoader();
    }

    public void queueAll() {
        for (AtlasAsset asset : AtlasAsset.values()) {
            services.getAssetManager().queue(asset);
        }
        for (SoundAsset soundAsset : SoundAsset.values()) {
            services.getAssetManager().queue(soundAsset);
        }
        for (QuestJson questJson : services.getQuestRepository().loadAll().values()) {
            services.getAllQuests().put(questJson.questId(), questFactory.createQuestFromJson(questJson));
        }
        for (var entry : services.getDialogRepository().loadAll().entrySet()) {
            services.getAllDialogs().put(entry.getKey(), dialogLoader.load(entry.getValue()));
        }
        services.getAssetManager().queue(SkinAsset.DEFAULT);
    }

    public boolean update() {
        return services.getAssetManager().update();
    }
}
