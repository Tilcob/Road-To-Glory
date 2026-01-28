package com.github.tilcob.game.flow.commands;

import com.github.tilcob.game.event.DialogGiveItemEvent;
import com.github.tilcob.game.event.DialogGiveMoneyEvent;
import com.github.tilcob.game.event.DialogIncCounterEvent;
import com.github.tilcob.game.event.DialogSetFlagEvent;
import com.github.tilcob.game.flow.CommandRegistry;
import com.github.tilcob.game.flow.FlowAction;

import java.util.List;

public final class DialogCommandModule {

    public void register(CommandRegistry registry) {

        registry.register("give_money", (call, ctx) -> {
            int amount = Integer.parseInt(call.arguments().get(0));
            return List.of(new FlowAction.EmitEvent(new DialogGiveMoneyEvent(ctx.player(), amount)));
        });

        registry.register("give_item", (call, ctx) -> {
            String itemId = call.arguments().get(0);
            int count = call.arguments().get(1) != null ? Integer.parseInt(call.arguments().get(1)) : 1;
            return List.of(new FlowAction.EmitEvent(new DialogGiveItemEvent(ctx.player(), itemId, count)));
        });

        registry.register("set_flag", (call, ctx) -> {
            String flag = call.arguments().get(0);
            boolean value = call.arguments().size() < 2 || Boolean.parseBoolean(call.arguments().get(1));
            return List.of(new FlowAction.EmitEvent(new DialogSetFlagEvent(ctx.player(), flag, value)));
        });

        registry.register("inc_counter", (call, ctx) -> {
            String counter = call.arguments().get(0);
            int delta = call.arguments().size() >= 2 ? Integer.parseInt(call.arguments().get(1)) : 1;
            return List.of(new FlowAction.EmitEvent(new DialogIncCounterEvent(ctx.player(), counter, delta)));
        });
    }
}
