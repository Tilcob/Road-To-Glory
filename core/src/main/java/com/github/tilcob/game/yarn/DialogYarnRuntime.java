package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.flow.*;

import java.util.List;
import java.util.Optional;

public class DialogYarnRuntime {
    private final YarnRuntime runtime;
    private final CommandRegistry commandRegistry;
    private final FlowExecutor flowExecutor;

    public DialogYarnRuntime(YarnRuntime runtime, CommandRegistry commandRegistry, FlowExecutor flowExecutor) {
        this.runtime = runtime;
        this.commandRegistry = commandRegistry;
        this.flowExecutor = flowExecutor;
    }

    public boolean tryExecuteCommandLine(Entity player, String line, CommandCall.SourcePos source) {
        Optional<CommandCall> callOptional = runtime.parseCommandLine(line, source);
        if (callOptional.isEmpty()) return false;

        CommandCall call = callOptional.get();
        List<FlowAction> actions = commandRegistry.dispatch(call, new FlowContext(player));
        return true;
    }

    public boolean tryExecuteCommandLine(Entity player, String line) {
        return tryExecuteCommandLine(player, line, CommandCall.SourcePos.unknown());
    }
}
