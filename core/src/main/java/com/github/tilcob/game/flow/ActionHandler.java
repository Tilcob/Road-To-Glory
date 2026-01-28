package com.github.tilcob.game.flow;

@FunctionalInterface
public interface ActionHandler<T extends FlowAction> {

    void handle(T action, FlowExecutor.RuntimeContext runtimeContext);
}
