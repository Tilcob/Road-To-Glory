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
import com.github.tilcob.game.quest.QuestDefinition;
import com.github.tilcob.game.quest.QuestFactory;
import com.github.tilcob.game.quest.QuestYarnRegistry;

import java.util.Map;

public class GameLoader {
    private static final String TAG = GameLoader.class.getSimpleName();

    private final GameServices services;
    private final QuestFactory questFactory;
    private final YarnDialogLoader dialogLoader;
    private final QuestYarnRegistry questRegistry;
    private final DialogRepository dialogRepository;

    public GameLoader(GameServices services) {
        this.services = services;
        this.questFactory = new QuestFactory(services.getEventBus(), services.getQuestYarnRegistry());
        this.dialogLoader = new YarnDialogLoader();
        this.questRegistry = services.getQuestYarnRegistry();
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
        Map<String, QuestDefinition> questDefinitions = questRegistry.loadAll();
        if (questDefinitions.isEmpty()) Gdx.app.error(TAG, "No quest definitions loaded from repository.");

        for (QuestDefinition questDefinition : questDefinitions.values()) {
            if (questDefinition == null || questDefinition.questId() == null || questDefinition.questId().isBlank()) {
                Gdx.app.error(TAG, "Encountered quest definition without a valid questId.");
                continue;
            }
            services.getAllQuests().put(questDefinition.questId(), questFactory.createQuest(questDefinition));
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
