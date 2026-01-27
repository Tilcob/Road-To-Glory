package com.github.tilcob.game.flow;

import java.util.List;

@FunctionalInterface
public interface CommandHandler {
    List<FlowAction> handle(CommandCall call, FlowContext context);
}
