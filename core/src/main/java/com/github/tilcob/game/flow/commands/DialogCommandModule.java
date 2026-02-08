package com.github.tilcob.game.flow.commands;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.component.OverheadIndicator;
import com.github.tilcob.game.entity.EntityLookup;
import com.github.tilcob.game.event.*;
import com.github.tilcob.game.flow.CommandRegistry;
import com.github.tilcob.game.flow.FlowAction;

import java.util.List;
import java.util.function.Supplier;

public final class DialogCommandModule {
    private final Supplier<EntityLookup> entityLookup;

    public DialogCommandModule() {
        this(null);
    }

    public DialogCommandModule(Supplier<EntityLookup> entityLookup) {
        this.entityLookup = entityLookup;
    }

    public void register(CommandRegistry registry) {

        registry.register("give_money", (call, ctx) -> {
            int amount = Integer.parseInt(call.arguments().get(0));
            return List.of(new FlowAction.EmitEvent(new DialogGiveMoneyEvent(ctx.player(), amount)));
        });

        registry.register("give_item", (call, ctx) -> {
            String itemId = argumentAt(call.arguments(), 0);
            String countArg = argumentAt(call.arguments(), 1);
            int count = countArg != null ? Integer.parseInt(countArg) : 1;
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

        registry.register("play_indicator", (call, ctx) -> {
            String firstArg = argumentAt(call.arguments(), 0);
            String secondArg = argumentAt(call.arguments(), 1);
            String thirdArg = argumentAt(call.arguments(), 2);

            OverheadIndicator.OverheadIndicatorType indicatorType = parseIndicatorType(firstArg);
            Entity target = ctx.npc();
            Float durationSeconds;

            if (indicatorType != null) {
                durationSeconds = parseDurationSeconds(secondArg);
            } else {
                target = resolveEntity(ctx.player(), ctx.npc(), firstArg);
                indicatorType = parseIndicatorType(secondArg);
                durationSeconds = parseDurationSeconds(thirdArg);
            }

            return List.of(new FlowAction.EmitEvent(new PlayIndicatorEvent(ctx.player(), target,
                indicatorType, durationSeconds)));
        });
    }

    private String argumentAt(List<String> arguments, int index) {
        return index >= 0 && index < arguments.size() ? arguments.get(index) : null;
    }

    private Float parseDurationSeconds(String durationSeconds) {
        if (durationSeconds == null || durationSeconds.isBlank()) return null;
        try {
            return Math.max(0f, Float.parseFloat(durationSeconds));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private OverheadIndicator.OverheadIndicatorType parseIndicatorType(String type) {
        if (type == null || type.isBlank()) return null;

        try {
            return OverheadIndicator.OverheadIndicatorType.valueOf(type.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Entity resolveEntity(Entity player, Entity fallbackNpc, String entityId) {
        if (entityId == null || entityId.isBlank()) return fallbackNpc;

        EntityLookup lookup = entityLookup == null ? null : entityLookup.get();
        if (lookup != null) {
            Entity resolved = lookup.find(entityId);
            if (resolved != null) return resolved;
        }
        if ("player".equalsIgnoreCase(entityId)) return player;
        return null;
    }
}
