package com.github.tilcob.game.flow;

import com.github.tilcob.game.audio.AudioManager;
import com.github.tilcob.game.entity.EntityLookup;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.flow.commands.*;
import com.github.tilcob.game.flow.functions.QuestFunctionModule;
import com.github.tilcob.game.quest.QuestLifecycleService;

import java.util.function.Supplier;

public final class FlowBootstrap {

    private final FlowTrace trace;
    private final FlowExecutor executor;
    private final CommandRegistry commands;
    private final FunctionRegistry functions;

    private FlowBootstrap(FlowTrace trace,
                          FlowExecutor executor,
                          CommandRegistry commands,
                          FunctionRegistry functions) {
        this.trace = trace;
        this.executor = executor;
        this.commands = commands;
        this.functions = functions;
    }

    public FlowTrace trace() { return trace; }
    public FlowExecutor executor() { return executor; }
    public CommandRegistry commands() { return commands; }
    public FunctionRegistry functions() { return functions; }

    public static FlowBootstrap create(GameEventBus eventBus,
                                       QuestLifecycleService questLifecycleService,
                                       AudioManager audioManager,
                                       Supplier<EntityLookup> entityLookup) {

        FlowTrace trace = new FlowTrace(200);
        FlowExecutor executor = new FlowExecutor(eventBus, trace);
        CommandRegistry commandRegistry = new CommandRegistry();
        FunctionRegistry functionRegistry = new FunctionRegistry();
        new DialogCommandModule(entityLookup).register(commandRegistry);
        new QuestCommandModule().register(commandRegistry);
        new CutsceneCommandModule(entityLookup).register(commandRegistry);
        new QuestFunctionModule().register(functionRegistry);

        new DialogCommandHandler(eventBus);
        new QuestCommandHandler(eventBus, questLifecycleService);
        new CutsceneCommandHandler(eventBus, audioManager);
        new GeneralCommandHandler(eventBus);

        return new FlowBootstrap(trace, executor, commandRegistry, functionRegistry);
    }
}
