package com.github.tilcob.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.github.tilcob.game.assets.AtlasAsset;
import com.github.tilcob.game.assets.SkinAsset;
import com.github.tilcob.game.assets.SoundAsset;
import com.github.tilcob.game.dialog.DialogRepository;
import com.github.tilcob.game.dialog.YarnDialogLoader;
import com.github.tilcob.game.item.ItemDefinition;
import com.github.tilcob.game.item.ItemDefinitionRegistry;
import com.github.tilcob.game.item.ItemLoader;
import com.github.tilcob.game.quest.QuestFactory;
import com.github.tilcob.game.quest.QuestJson;
import com.github.tilcob.game.quest.QuestRepository;

import java.util.Map;

public class GameLoader {
    private static final String TAG = GameLoader.class.getSimpleName();

    private final GameServices services;
    private final QuestFactory questFactory;
    private final YarnDialogLoader dialogLoader;
    private final QuestRepository questRepository;
    private final DialogRepository dialogRepository;

    public GameLoader(GameServices services) {
        this.services = services;
        this.questFactory = new QuestFactory(services.getEventBus(), services.getQuestRepository());
        this.dialogLoader = new YarnDialogLoader();
        this.questRepository = services.getQuestRepository();
        this.dialogRepository = services.getDialogRepository();
    }

    public void queueAll() {
        for (AtlasAsset asset : AtlasAsset.values()) {
            services.getAssetManager().queue(asset);
        }
        for (SoundAsset soundAsset : SoundAsset.values()) {
            services.getAssetManager().queue(soundAsset);
        }
        for (ItemDefinition definition : ItemLoader.loadAll()) {
            ItemDefinitionRegistry.register(definition);
        }
        Map<String, QuestJson> questDefinitions = questRepository.loadAll();
        if (questDefinitions.isEmpty()) Gdx.app.error(TAG, "No quest definitions loaded from repository.");

        for (QuestJson questJson : questDefinitions.values()) {
            if (questJson == null || questJson.questId() == null || questJson.questId().isBlank()) {
                Gdx.app.error(TAG, "Encountered quest definition without a valid questId.");
                continue;
            }
            services.getAllQuests().put(questJson.questId(), questFactory.createQuestFromJson(questJson));
        }
        Map<String, FileHandle> dialogFiles = dialogRepository.loadAll();
        if (dialogFiles.isEmpty()) {
            Gdx.app.error(TAG, "No dialog files loaded from repository.");
        }
        for (var entry : dialogFiles.entrySet()) {
            String npcId = entry.getKey();
            FileHandle dialogFile = entry.getValue();
            if (npcId == null || npcId.isBlank()) {
                Gdx.app.error(TAG, "Encountered dialog entry without an npcId.");
                continue;
            }
            if (dialogFile == null) {
                Gdx.app.error(TAG, "Dialog file missing for npcId: " + npcId);
                continue;
            }
            services.getAllDialogs().put(npcId, dialogLoader.load(dialogFile));
        }
        services.getAssetManager().queue(SkinAsset.DEFAULT);
    }

    public boolean update() {
        return services.getAssetManager().update();
    }
}
