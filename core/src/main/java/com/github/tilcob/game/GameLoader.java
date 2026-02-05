package com.github.tilcob.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.github.tilcob.game.assets.AtlasAsset;
import com.github.tilcob.game.assets.SkinAsset;
import com.github.tilcob.game.assets.SoundAsset;
import com.github.tilcob.game.component.OverheadIndicator;
import com.github.tilcob.game.cutscene.CutsceneRepository;
import com.github.tilcob.game.cutscene.YarnCutsceneLoader;
import com.github.tilcob.game.dialog.DialogRepository;
import com.github.tilcob.game.dialog.YarnDialogLoader;
import com.github.tilcob.game.indicator.OverheadIndicatorRegistry;
import com.github.tilcob.game.item.ItemDefinition;
import com.github.tilcob.game.item.ItemDefinitionRegistry;
import com.github.tilcob.game.item.ItemLoader;
import com.github.tilcob.game.quest.QuestDefinition;
import com.github.tilcob.game.quest.QuestFactory;
import com.github.tilcob.game.quest.QuestYarnRegistry;

import java.util.Map;
import java.util.function.BiConsumer;

public class GameLoader {
    private static final String TAG = GameLoader.class.getSimpleName();

    private final GameServices services;
    private final QuestFactory questFactory;
    private final YarnDialogLoader dialogLoader;
    private final YarnCutsceneLoader cutsceneLoader;
    private final QuestYarnRegistry questRegistry;
    private final DialogRepository dialogRepository;
    private final CutsceneRepository cutsceneRepository;

    public GameLoader(GameServices services) {
        this.services = services;
        this.questFactory = new QuestFactory(services.getQuestYarnRegistry());
        this.dialogLoader = new YarnDialogLoader();
        this.cutsceneLoader = new YarnCutsceneLoader();
        this.questRegistry = services.getQuestYarnRegistry();
        this.dialogRepository = services.getDialogRepository();
        this.cutsceneRepository = services.getCutsceneRepository();
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
        Map<String, FileHandle> questFiles = questRegistry.getQuestFiles();
        loadFileHandles(
            questFiles,
            "No quest yarn files loaded from repository.",
            "Encountered quest file entry without a questId.",
            "Quest file missing for questId: ",
            (questId, questFile) -> services.getAllQuestDialogs().put(questId, dialogLoader.load(questFile))
        );

        Map<String, FileHandle> dialogFiles = dialogRepository.loadAll();
        loadFileHandles(
            dialogFiles,
            "No dialog files loaded from repository.",
            "Encountered dialog entry without an npcId.",
            "Dialog file missing for npcId: ",
            (npcId, dialogFile) -> services.getAllDialogs().put(npcId, dialogLoader.load(dialogFile))
        );

        Map<String, FileHandle> cutsceneFiles = cutsceneRepository.loadAll();
        if (cutsceneFiles.isEmpty()) {
            Gdx.app.error(TAG, "No cutscene files loaded from repository.");
        }
        loadFileHandles(
            cutsceneFiles,
            "No cutscene files loaded from repository.",
            "Encountered cutscene entry without a cutsceneId.",
            "Cutscene file missing for cutsceneId: ",
            (cutsceneId, cutsceneFile) -> services.getAllCutscenes().put(
                cutsceneId,
                cutsceneLoader.load(cutsceneId, cutsceneFile)
            )
        );
        services.getAssetManager().queue(SkinAsset.DEFAULT);
        OverheadIndicatorRegistry.clear();

        OverheadIndicatorRegistry.register(
            OverheadIndicator.OverheadIndicatorType.QUEST_AVAILABLE,
            AtlasAsset.INDICATORS,
            "quest_available"
        );
        OverheadIndicatorRegistry.register(
            OverheadIndicator.OverheadIndicatorType.TALKING,
            AtlasAsset.INDICATORS,
            "speech_indicator",
            .12f,
            Animation.PlayMode.NORMAL
        );
    }

    private void loadFileHandles(
        Map<String, FileHandle> fileHandles,
        String emptyMessage,
        String missingIdMessage,
        String missingFileMessagePrefix,
        BiConsumer<String, FileHandle> fileConsumer
    ) {
        if (fileHandles.isEmpty()) Gdx.app.error(TAG, emptyMessage);

        for (var entry : fileHandles.entrySet()) {
            String id = entry.getKey();
            FileHandle handle = entry.getValue();
            if (id == null || id.isBlank()) {
                Gdx.app.error(TAG, missingIdMessage);
                continue;
            }
            if (fileConsumer == null) {
                Gdx.app.error(TAG, missingFileMessagePrefix + id);
                continue;
            }
            fileConsumer.accept(id, handle);
        }
    }

    public boolean update() {
        return services.getAssetManager().update();
    }
}
