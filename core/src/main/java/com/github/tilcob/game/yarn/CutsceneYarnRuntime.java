package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.flow.*;
import com.github.tilcob.game.flow.commands.CutsceneCommandResult;

import java.util.List;
import java.util.Optional;

public class CutsceneYarnRuntime {
    private static final String TAG = CutsceneYarnRuntime.class.getSimpleName();

    private final YarnRuntime runtime;
    private final CommandRegistry registry;
    private final FlowExecutor executor;

    public CutsceneYarnRuntime(YarnRuntime runtime, CommandRegistry registry, FlowExecutor executor) {
        this.runtime = runtime;
        this.registry = registry;
        this.executor = executor;
    }

    public CutsceneCommandResult executeLine(Entity player, String line, CommandCall.SourcePos source) {
        Optional<CommandCall> callOptional = runtime.parseCommandLine(line, source);
        if (callOptional.isEmpty()) return CutsceneCommandResult.notACommand();

        CommandCall call = callOptional.get();
        switch (call.command()) {
            case "wait" -> {
                float seconds = parseFloat(call, 0, 0f);
                return CutsceneCommandResult.waitSeconds(seconds);
            }
            case "wait_for_camera" -> {
                return CutsceneCommandResult.waitForCamera();
            }
            case "wait_for_move" -> {
                return CutsceneCommandResult.waitForMove();
            }
            case "wait_for_dialog" -> {
                return CutsceneCommandResult.waitForDialog();
            }
            default -> {
                List<FlowAction> actions = registry.dispatch(call, new FlowContext(player));
                executor.execute(actions);
                return CutsceneCommandResult.commandExecuted();
            }
        }
    }

    public CutsceneCommandResult executeLine(Entity player, String line) {
        return executeLine(player, line, CommandCall.SourcePos.unknown());
    }

    private static float parseFloat(CommandCall call, int argIndex, float fallback) {
        if (call.arguments().size() <= argIndex) return fallback;
        return Float.parseFloat(call.arguments().get(argIndex));
    }

    private float parseFloat(String raw, float fallback) {
        try {
            return Float.parseFloat(raw);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
