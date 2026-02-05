package com.github.tilcob.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.github.tilcob.game.assets.AtlasAsset;
import com.github.tilcob.game.assets.SkinAsset;
import com.github.tilcob.game.assets.SoundAsset;
import com.github.tilcob.game.component.OverheadIndicator;
import com.github.tilcob.game.config.Constants;
import com.github.tilcob.game.cutscene.CutsceneRepository;
import com.github.tilcob.game.cutscene.YarnCutsceneLoader;
import com.github.tilcob.game.dialog.DialogRepository;
import com.github.tilcob.game.dialog.YarnDialogLoader;
import com.github.tilcob.game.indicator.IndicatorVisualDef;
import com.github.tilcob.game.indicator.OverheadIndicatorRegistry;
import com.github.tilcob.game.input.Command;
import com.github.tilcob.game.input.InputBindings;
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
    private final InputBindings inputBindings;

    public GameLoader(GameServices services, InputBindings inputBindings) {
        this.services = services;
        this.questFactory = new QuestFactory(services.getQuestYarnRegistry());
        this.dialogLoader = new YarnDialogLoader();
        this.cutsceneLoader = new YarnCutsceneLoader();
        this.questRegistry = services.getQuestYarnRegistry();
        this.dialogRepository = services.getDialogRepository();
        this.cutsceneRepository = services.getCutsceneRepository();
        this.inputBindings = inputBindings;
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
            "quest_available",
            Constants.FRAME_DURATION,
            Animation.PlayMode.LOOP,
            new IndicatorVisualDef(1f, 0f, 0f, 0.15f, 2.5f, 0.08f, 3.0f)
        );
        OverheadIndicatorRegistry.register(
            OverheadIndicator.OverheadIndicatorType.QUEST_TURNING,
            AtlasAsset.INDICATORS,
            "quest_available",
            Constants.FRAME_DURATION,
            Animation.PlayMode.LOOP,
            new IndicatorVisualDef(1f, 0f, 0f, 0.20f, 2.8f, 0.10f, 3.2f)
        );
        OverheadIndicatorRegistry.register(
            OverheadIndicator.OverheadIndicatorType.INFO,
            AtlasAsset.INDICATORS,
            "speech_indicator",
            .12f,
            Animation.PlayMode.LOOP,
            new IndicatorVisualDef(1f, 0f, 0f, 0.10f, 2.3f, 0.06f, 2.8f)
        );
        OverheadIndicatorRegistry.register(
            OverheadIndicator.OverheadIndicatorType.MERCHANT,
            AtlasAsset.INDICATORS,
            "speech_indicator",
            .12f,
            Animation.PlayMode.LOOP,
            new IndicatorVisualDef(1f, 0f, 0f, 0.10f, 2.3f, 0.06f, 2.8f)
        );
        OverheadIndicatorRegistry.register(
            OverheadIndicator.OverheadIndicatorType.TALK_AVAILABLE,
            AtlasAsset.INDICATORS,
            "speech_indicator",
            .12f,
            Animation.PlayMode.LOOP,
            new IndicatorVisualDef(1f, 0f, 0f, 0.10f, 2.3f, 0.06f, 2.8f)
        );
        OverheadIndicatorRegistry.register(
            OverheadIndicator.OverheadIndicatorType.TALK_IN_RANGE,
            AtlasAsset.INDICATORS,
            "speech_indicator",
            .12f,
            Animation.PlayMode.LOOP,
            new IndicatorVisualDef(1f, 0f, 0f, 0.12f, 2.5f, 0.07f, 2.8f)
        );
        OverheadIndicatorRegistry.register(
            OverheadIndicator.OverheadIndicatorType.TALK_BUSY,
            AtlasAsset.INDICATORS,
            "speech_indicator",
            .10f,
            Animation.PlayMode.LOOP,
            new IndicatorVisualDef(1f, 0f, 0f, 0.18f, 2.8f, 0.10f, 3.2f)
        );
        OverheadIndicatorRegistry.register(
            OverheadIndicator.OverheadIndicatorType.TALK_CHOICE,
            AtlasAsset.INDICATORS,
            "speech_indicator",
            .10f,
            Animation.PlayMode.LOOP,
            new IndicatorVisualDef(1f, 0f, 0f, 0.20f, 3.0f, 0.10f, 3.2f)
        );
        OverheadIndicatorRegistry.register(
            OverheadIndicator.OverheadIndicatorType.INTERACT_HINT,
            AtlasAsset.INDICATORS,
            "key_" + inputBindings.getPrimaryBindingLabel(Command.INTERACT).toLowerCase(),
            .12f,
            Animation.PlayMode.LOOP,
            new IndicatorVisualDef(.3f, 0f, 0f, 0.08f, 2.6f, 0.05f, 3.0f)
        );
        OverheadIndicatorRegistry.register(
            OverheadIndicator.OverheadIndicatorType.DANGER,
            AtlasAsset.INDICATORS,
            "speech_indicator",
            .10f,
            Animation.PlayMode.LOOP,
            new IndicatorVisualDef(1f, 0f, 0f, 0.20f, 3.0f, 0.12f, 3.4f)
        );
        OverheadIndicatorRegistry.register(
            OverheadIndicator.OverheadIndicatorType.ANGRY,
            AtlasAsset.INDICATORS,
            "speech_indicator",
            .10f,
            Animation.PlayMode.LOOP,
            new IndicatorVisualDef(1f, 0f, 0f, 0.20f, 3.0f, 0.12f, 3.4f)
        );
        OverheadIndicatorRegistry.register(
            OverheadIndicator.OverheadIndicatorType.TALKING,
            AtlasAsset.INDICATORS,
            "speech_indicator",
            .12f,
            Animation.PlayMode.NORMAL,
            new IndicatorVisualDef(1f, 0f, 0f, 0f, 2.5f, 0f, 3.0f)
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
