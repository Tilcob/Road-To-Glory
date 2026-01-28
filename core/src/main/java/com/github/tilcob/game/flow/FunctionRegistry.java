package com.github.tilcob.game.flow;

import java.util.HashMap;
import java.util.Map;

public final class FunctionRegistry {
    private final Map<String, FunctionHandler> handlers = new HashMap<>();

    public void register(String function, FunctionHandler handler) {
        if (function == null || function.isBlank()) throw new IllegalArgumentException("Function must be non-empty.");
        if (handler == null) throw new IllegalArgumentException("Handler must not be null.");
        handlers.put(function, handler);
    }

    public boolean has(String function) {
        return handlers.containsKey(function);
    }

    public Object evaluate(FunctionCall call, FlowContext context) {
        FunctionHandler handler = handlers.get(call.function());
        if (handler == null) throw new IllegalStateException("Unknown function: " + call.function() + " @ " + call.source());
        return handler.eval(call, context);
    }
}
