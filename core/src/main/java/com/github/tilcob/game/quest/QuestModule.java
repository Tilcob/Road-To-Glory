package com.github.tilcob.game.quest;

import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.config.ContentPaths;
import com.github.tilcob.game.dialog.DialogData;
import com.github.tilcob.game.entity.EntityLookup;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.flow.FlowBootstrap;
import com.github.tilcob.game.yarn.QuestYarnRuntime;
import com.github.tilcob.game.yarn.YarnRuntime;

import java.util.Map;
import java.util.function.Supplier;

public final class QuestModule {
    private QuestModule() {
    }

    public static QuestServices create(
        GameEventBus eventBus,
        Map<String, DialogData> allDialogs,
        AudioManager audioManager,
        Supplier<EntityLookup> entityLookupSupplier
    ) {
        QuestYarnRegistry questYarnRegistry = new QuestYarnRegistry(ContentPaths.QUESTS_INDEX);
        QuestLifecycleService questLifecycleService = new QuestLifecycleService(eventBus, questYarnRegistry, allDialogs);
        QuestRewardService questRewardService = new QuestRewardService(eventBus, questYarnRegistry);
        FlowBootstrap flowBootstrap = FlowBootstrap.create(eventBus, questLifecycleService, audioManager, entityLookupSupplier);
        YarnRuntime runtime = new YarnRuntime();
        QuestYarnRuntime questYarnRuntime = new QuestYarnRuntime(
            runtime,
            questYarnRegistry,
            flowBootstrap.commands(),
            flowBootstrap.executor(),
            flowBootstrap.functions());
        questLifecycleService.setQuestYarnRuntime(questYarnRuntime);
        QuestManager questManager = new QuestManager(questYarnRuntime);
        questLifecycleService.setQuestManager(questManager);

        return new QuestServices(
            questManager,
            questYarnRegistry,
            questLifecycleService,
            questRewardService,
            questYarnRuntime,
            flowBootstrap,
            runtime);
    }

    public record QuestServices(
        QuestManager questManager,
        QuestYarnRegistry questYarnRegistry,
        QuestLifecycleService questLifecycleService,
        QuestRewardService questRewardService,
        QuestYarnRuntime questYarnRuntime,
        FlowBootstrap flowBootstrap,
        YarnRuntime yarnRuntime
    ) {
    }
}
