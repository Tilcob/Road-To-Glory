package com.github.tilcob.game.flow;

import com.github.tilcob.game.event.GameEventBus;

import java.util.List;

public final class FlowExecutor {
    private final RuntimeContext runtimeContext;

    public FlowExecutor(GameEventBus eventBus, FlowTrace trace) {
        this.runtimeContext = new RuntimeContext(eventBus, trace);
    }

    public void execute(List<FlowAction> actions) {
        if (actions == null || actions.isEmpty()) return;

        for (FlowAction action : actions) {
            if (action == null) continue;
            runtimeContext.trace.record(action);

            if (action instanceof FlowAction.EmitEvent emitEvent) {
                runtimeContext.eventBus.fire(emitEvent.event());
            } else if (action instanceof FlowAction.Log log) {
                runtimeContext.trace.record(new FlowAction.Log("[FLOW] " + log.message()));
            } else if (action instanceof FlowAction.Noop) {

            } else {
                throw new IllegalArgumentException("Unknown FlowAction: " + action);
            }
        }
    }

    public record RuntimeContext(
        GameEventBus eventBus,
        FlowTrace trace
    ) {}
}
