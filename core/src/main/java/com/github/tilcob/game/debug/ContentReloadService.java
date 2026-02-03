package com.github.tilcob.game.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.github.tilcob.game.GameServices;
import com.github.tilcob.game.config.ContentPaths;
import com.github.tilcob.game.cutscene.CutsceneData;
import com.github.tilcob.game.cutscene.YarnCutsceneLoader;
import com.github.tilcob.game.dialog.DialogData;
import com.github.tilcob.game.dialog.YarnDialogLoader;
import com.github.tilcob.game.item.ItemDefinition;
import com.github.tilcob.game.item.ItemDefinitionRegistry;
import com.github.tilcob.game.item.ItemLoader;
import com.github.tilcob.game.quest.Quest;
import com.github.tilcob.game.quest.QuestDefinition;
import com.github.tilcob.game.quest.QuestFactory;
import com.github.tilcob.game.quest.QuestYarnRegistry;

import java.util.List;
import java.util.Map;

public class ContentReloadService {
    private static final String TAG = ContentReloadService.class.getSimpleName();

    private final GameServices services;
    private final YarnDialogLoader dialogLoader = new YarnDialogLoader();
    private final YarnCutsceneLoader cutsceneLoader = new YarnCutsceneLoader();

    public ContentReloadService(GameServices services) {
        this.services = services;
    }

    public void reloadAll() {
        try { reloadItems(); } catch (Exception e) { Gdx.app.error(TAG, "Reload items failed", e); }
        try { reloadQuestDefinitions(); } catch (Exception e) { Gdx.app.error(TAG, "Reload quest definitions failed", e); }
        try { reloadQuestDialogs(); } catch (Exception e) { Gdx.app.error(TAG, "Reload quest dialogs failed", e); }
        try { reloadNpcDialogs(); } catch (Exception e) { Gdx.app.error(TAG, "Reload npc dialogs failed", e); }
        try { reloadCutscenes(); } catch (Exception e) { Gdx.app.error(TAG, "Reload cutscenes failed", e); }
    }

    public void reloadItems() {
        ItemDefinitionRegistry.clear();
        List<ItemDefinition> defs = ItemLoader.loadAll();
        for (ItemDefinition d : defs) {
            ItemDefinitionRegistry.register(d);
        }
        Gdx.app.log(TAG, "Reloaded items: " + defs.size());
    }

    public void reloadQuestDefinitions() {
        QuestYarnRegistry registry = services.getQuestYarnRegistry();
        Map<String, QuestDefinition> defs = registry.loadAll();

        QuestFactory factory = new QuestFactory(registry);

        Map<String, Quest> allQuests = services.getAllQuests();
        for (QuestDefinition def : defs.values()) {
            if (def == null || def.questId() == null || def.questId().isBlank()) continue;

            Quest oldQuest = allQuests.get(def.questId());
            Quest fresh = factory.create(def.questId());

            if (oldQuest != null) {
                fresh.setCurrentStep(oldQuest.getCurrentStep());
                fresh.setRewardClaimed(oldQuest.isRewardClaimed());
                fresh.setCompletionNotified(oldQuest.isCompletionNotified());
            }

            allQuests.put(def.questId(), fresh);
        }

        Gdx.app.log(TAG, "Reloaded quest definitions: " + defs.size());
    }

    public void reloadQuestDialogs() {
        Map<String, DialogData> questDialogs = services.getAllQuestDialogs();
        questDialogs.clear();

        Map<String, FileHandle> questFiles = services.getQuestYarnRegistry().getQuestFiles();
        for (var e : questFiles.entrySet()) {
            String questId = e.getKey();
            FileHandle questFile = e.getValue();
            if (questId == null || questId.isBlank()) continue;
            if (questFile == null || !questFile.exists()) continue;
            questDialogs.put(questId, dialogLoader.load(questFile));
        }

        Gdx.app.log(TAG, "Reloaded quest dialogs: " + questDialogs.size());
    }

    public void reloadNpcDialogs() {
        Map<String, DialogData> dialogs = services.getAllDialogs();
        dialogs.clear();

        Map<String, FileHandle> files = services.getDialogRepository().loadAll();
        for (var e : files.entrySet()) {
            String npcId = e.getKey();
            FileHandle file = e.getValue();
            if (npcId == null || npcId.isBlank()) continue;
            if (file == null || !file.exists()) continue;
            dialogs.put(npcId, dialogLoader.load(file));
        }

        Gdx.app.log(TAG, "Reloaded npc dialogs: " + dialogs.size());
    }

    public void reloadCutscenes() {
        Map<String, CutsceneData> cutscenes = services.getAllCutscenes();
        cutscenes.clear();

        Map<String, FileHandle> files = services.getCutsceneRepository().loadAll();
        for (var e : files.entrySet()) {
            String id = e.getKey();
            FileHandle file = e.getValue();
            if (id == null || id.isBlank()) continue;
            if (file == null || !file.exists()) continue;
            cutscenes.put(id, cutsceneLoader.load(id, file));
        }

        Gdx.app.log(TAG, "Reloaded cutscenes: " + cutscenes.size());
    }

    public List<FileHandle> collectWatchFiles() {
        return List.of(
            Gdx.files.internal(ContentPaths.ITEMS_INDEX),
            Gdx.files.internal(ContentPaths.QUESTS_INDEX),
            Gdx.files.internal(ContentPaths.DIALOGS_INDEX),
            Gdx.files.internal(ContentPaths.CUTSCENES_INDEX),
            Gdx.files.internal(ContentPaths.AUDIO_INDEX),
            Gdx.files.internal(ContentPaths.MAPS_INDEX)
        );
    }
}
