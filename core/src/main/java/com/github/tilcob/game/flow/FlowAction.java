package com.github.tilcob.game.flow;

public sealed interface FlowAction permits
    FlowAction.EmitEvent,
    FlowAction.Log,
    FlowAction.Noop {

    record EmitEvent(Object event) implements FlowAction {
        public EmitEvent {
            if (event == null) throw new IllegalArgumentException("event is null");
        }
    }

    record Log(String message) implements FlowAction {}

    record Noop() implements FlowAction {}
}
