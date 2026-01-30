package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;
import com.github.tilcob.game.event.GameEventBus;
import com.github.tilcob.game.flow.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class YarnEqualitySemanticsTest {
    private Entity player;
    private BaseYarnRuntime runtime;

    @BeforeEach
    void setup() {
        player = new Entity();
        FunctionRegistry functions = new FunctionRegistry();
        runtime = new BaseYarnRuntime(
            new YarnRuntime(),
            new CommandRegistry(),
            new FlowExecutor(new GameEventBus(), new FlowTrace(200)),
            functions) {};
    }

    @Test void number_equals_number() {
        runtime.setVariable(player, "x", 3);
        assertTrue(runtime.evaluateCondition(player, "x == 3", CommandCall.SourcePos.unknown()));
    }

    @Test void number_equals_numeric_string() {
        runtime.setVariable(player, "x", 3);
        assertTrue(runtime.evaluateCondition(player, "x == \"3\"", CommandCall.SourcePos.unknown()));
    }

    @Test void bool_equals_string() {
        runtime.setVariable(player, "b", true);
        assertTrue(runtime.evaluateCondition(player, "b == \"true\"", CommandCall.SourcePos.unknown()));
        assertFalse(runtime.evaluateCondition(player, "b == \"yes\"", CommandCall.SourcePos.unknown()));
    }

    @Test void string_equals_string_case_sensitive() {
        runtime.setVariable(player, "s", "Hi");
        assertTrue(runtime.evaluateCondition(player, "s == \"Hi\"", CommandCall.SourcePos.unknown()));
        assertFalse(runtime.evaluateCondition(player, "s == \"hi\"", CommandCall.SourcePos.unknown()));
    }
}
