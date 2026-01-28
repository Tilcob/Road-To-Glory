package com.github.tilcob.game.flow.commands;

import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.flow.CommandRegistry;
import com.github.tilcob.game.quest.QuestLifecycleService;
import com.github.tilcob.game.yarn.EntityLookup;

import java.util.function.Supplier;

public final class InitCommands {

    public InitCommands(GameEventBus eventBus,
                        Supplier<EntityLookup> entityLookup,
                        AudioManager audioManager,
                        CommandRegistry registry,
                        QuestLifecycleService questLifecycleService) {
        new DialogCommandHandler(eventBus);
        new DialogCommandModule().register(registry);
        new CutsceneCommandHandler(eventBus, audioManager);
        new CutsceneCommandModule(entityLookup).register(registry);
        new QuestCommandHandler(eventBus,questLifecycleService);
        new QuestCommandModule().register(registry);
    }
}
