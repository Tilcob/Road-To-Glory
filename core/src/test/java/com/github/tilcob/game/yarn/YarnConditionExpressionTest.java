package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.flow.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class YarnConditionExpressionTest {
    private Entity player;
    private BaseYarnRuntime runtime;

    @BeforeEach
    void setup() {
        player = new Entity();
        FunctionRegistry functionRegistry = new FunctionRegistry();

        functionRegistry.register("has_flag", (call, ctx) -> {
            String flag = call.arguments().get(0);
            Object v = runtime.getVariable(ctx.player(), flag);
            return v instanceof Boolean b && b;
        });

        runtime = new BaseYarnRuntime(
            new YarnRuntime(),
            new CommandRegistry(),
            new FlowExecutor(new GameEventBus(), new FlowTrace(200)),
            functionRegistry
        ) {};
    }

    @Test
    void complex_numeric_and_logical_expression_works() {
        runtime.setVariable(player, "$eventAmount", 3);
        runtime.setVariable(player, "$eventType", "COLLECT");

        boolean result = runtime.evaluateCondition(
            player,
            "$eventAmount >= 3 && ($eventType == \"COLLECT\" || $eventType == \"KILL\")",
            CommandCall.SourcePos.unknown()
        );

        assertTrue(result, "Expected complex && / || expression to evaluate to true");
    }

    @Test
    void complex_expression_fails_when_conditions_do_not_match() {
        runtime.setVariable(player, "$eventAmount", 1);
        runtime.setVariable(player, "$eventType", "COLLECT");

        boolean result = runtime.evaluateCondition(
            player,
            "$eventAmount >= 3 && ($eventType == \"COLLECT\" || $eventType == \"KILL\")",
            CommandCall.SourcePos.unknown()
        );

        assertFalse(result, "Expected expression to be false when eventAmount < 3");
    }

    @Test
    void unary_not_and_function_call_expression_works() {
        runtime.setVariable(player, "$guard_angry", false);

        boolean result = runtime.evaluateCondition(
            player,
            "!has_flag \"guard_angry\"",
            CommandCall.SourcePos.unknown()
        );

        assertTrue(result, "Expected !has_flag to evaluate to true");
    }

    @Test
    void else_case_when_flag_is_true() {
        runtime.setVariable(player, "$guard_angry", true);

        boolean result = runtime.evaluateCondition(
            player,
            "!has_flag \"guard_angry\"",
            CommandCall.SourcePos.unknown()
        );

        assertFalse(result, "Expected !has_flag to evaluate to false when flag is set");
    }
}
