package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.flow.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class YarnArithmeticExpressionTest {

    private Entity player;
    private BaseYarnRuntime runtime;

    @BeforeEach
    void setup() {
        player = new Entity();
        FunctionRegistry functionRegistry = new FunctionRegistry();
        functionRegistry.register("money", (call, ctx) -> runtime.getVariable(ctx.player(), "money"));

        runtime = new BaseYarnRuntime(
            new YarnRuntime(),
            new CommandRegistry(),
            new FlowExecutor(new GameEventBus(), new FlowTrace(200)),
            functionRegistry
        ) {};
    }

    @Test
    void respects_operator_precedence_mul_before_add() {
        runtime.setVariable(player, "money", 5);
        runtime.setVariable(player, "cost", 7);

        boolean r = runtime.evaluateCondition(
            player,
            "money() + 10 >= cost * 2",
            CommandCall.SourcePos.unknown()
        );
        assertTrue(r);
    }

    @Test
    void parentheses_override_precedence() {
        runtime.setVariable(player, "money", 5);
        runtime.setVariable(player, "cost", 7);

        boolean r = runtime.evaluateCondition(
            player,
            "(money() + 10) * 2 >= cost * 3",
            CommandCall.SourcePos.unknown()
        );

        // (5+10)*2=30  >= 21 -> true
        assertTrue(r);
    }

    @Test
    void unary_minus_works() {
        runtime.setVariable(player, "x", 3);

        boolean r = runtime.evaluateCondition(
            player,
            "-x == -3",
            CommandCall.SourcePos.unknown()
        );

        assertTrue(r);
    }

    @Test
    void division_and_subtraction_work() {
        runtime.setVariable(player, "a", 20);
        runtime.setVariable(player, "b", 5);

        boolean r = runtime.evaluateCondition(
            player,
            "a / b - 1 == 3",
            CommandCall.SourcePos.unknown()
        );
        assertTrue(r);
    }
}
