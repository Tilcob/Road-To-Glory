package com.github.tilcob.game.flow.commands;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.flow.CommandRegistry;
import com.github.tilcob.game.flow.FlowAction;

import java.util.List;

public final class DialogCommandModule {

    public void register(CommandRegistry registry) {

        registry.register("give_money", (call, ctx) -> {
            int amount = Integer.parseInt(call.arguments().get(0));
            // TODO: replace DialogGiveMoneyEvent with your existing event, if it already exists
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

    public record DialogGiveMoneyEvent(Entity player, int amount) {}
    public record DialogGiveItemEvent(Entity player, String itemId, int count) {}
    public record DialogSetFlagEvent(Entity player, String flag, boolean value) {}
    public record DialogIncCounterEvent(Entity player, String counter, int delta) {}
}
