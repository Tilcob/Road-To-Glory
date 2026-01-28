package com.github.tilcob.game.flow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CommandRegistry {
    private final Map<String, CommandHandler> handlers = new HashMap<>();

    public void register(String command, CommandHandler handler) {
        if (command == null || command.isBlank()) throw new IllegalArgumentException("Command must be non-empty.");
        if (handler == null) throw new IllegalArgumentException("Handler must not be null.");
        handlers.put(command, handler);
    }

    public boolean has(String command) {
        return handlers.containsKey(command);
    }

    public List<FlowAction> dispatch(CommandCall call, FlowContext context) {
        CommandHandler handler = handlers.get(call.command());
        if (handler == null)
            throw new IllegalArgumentException("No handler registered for command: " + call.command() + " @ " + call.source());
        return handler.handle(call, context);
    }
}
