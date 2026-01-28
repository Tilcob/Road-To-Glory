package com.github.tilcob.game.flow;

@FunctionalInterface
public interface FunctionHandler {
    Object eval(FunctionCall call, FlowContext context);
}
