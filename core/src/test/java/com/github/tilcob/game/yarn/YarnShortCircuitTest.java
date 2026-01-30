package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.flow.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class YarnShortCircuitTest {

    private Entity player;
    private BaseYarnRuntime runtime;

    private int called;

    @BeforeEach
    void setup() {
        player = new Entity();
        FunctionRegistry functions = new FunctionRegistry();

        called = 0;
        functions.register("side_effect", (call, ctx) -> {
            called++;
            return true;
        });

        runtime = new BaseYarnRuntime(
            new YarnRuntime(),
            new CommandRegistry(),
            new FlowExecutor(new GameEventBus(), new FlowTrace(200)),
            functions,
            true
        ) {};
    }

    @Test
    void or_short_circuits_when_left_is_true() {
        runtime.setVariable(player, "a", true);

        boolean r = runtime.evaluateCondition(
            player,
            "a || side_effect()",
            CommandCall.SourcePos.unknown()
        );

        assertTrue(r);
        assertEquals(0, called, "side_effect() must NOT be called due to OR short-circuit");
    }

    @Test
    void and_short_circuits_when_left_is_false() {
        runtime.setVariable(player, "a", false);

        boolean r = runtime.evaluateCondition(
            player,
            "a && side_effect()",
            CommandCall.SourcePos.unknown()
        );

        assertFalse(r);
        assertEquals(0, called, "side_effect() must NOT be called due to AND short-circuit");
    }
}
