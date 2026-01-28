package com.github.tilcob.game.flow.commands;

import com.github.tilcob.game.event.*;
import com.github.tilcob.game.flow.CommandRegistry;
import com.github.tilcob.game.flow.FlowAction;

import java.util.List;

public class QuestCommandModule {

    public void register(CommandRegistry registry) {
        registry.register("quest_start", (call, ctx) ->
            List.of(new FlowAction.EmitEvent(new RequestStartQuestEvent(ctx.player(), call.arguments().get(0))))
        );
        registry.register("quest_complete", (call, ctx) ->
            List.of(new FlowAction.EmitEvent(new RequestCompleteQuestEvent(ctx.player(), call.arguments().get(0))))
        );
        registry.register("quest_stage", (call, ctx) -> {
            String questId = call.arguments().get(0);
            int stage = parseInt(call.arguments().get(1), -1);
            return List.of(new FlowAction.EmitEvent(new RequestNextStageQuestEvent(ctx.player(), questId, stage)));
        });
        registry.register("set_flag", (call, ctx) -> {
            String flag = call.arguments().get(0);
            boolean value = call.arguments().size() < 2 || Boolean.parseBoolean(call.arguments().get(1));
            return List.of(new FlowAction.EmitEvent(new QuestSetFlagEvent(ctx.player(), flag, value)));
        });

        registry.register("inc_counter", (call, ctx) -> {
            String counter = call.arguments().get(0);
            int delta = call.arguments().size() >= 2 ? Integer.parseInt(call.arguments().get(1)) : 1;
            return List.of(new FlowAction.EmitEvent(new QuestIncCounterEvent(ctx.player(), counter, delta)));
        });
    }

    private int parseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
