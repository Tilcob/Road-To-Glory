package com.github.tilcob.game.yarn;

import com.github.tilcob.game.yarn.expression.ExpressionDiagnostics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpressionDiagnosticsTest {
    @Test
    void dumps_tokens_and_ast() {
        String expr = "(money() + 10) * 2 >= cost * 3";
        String tokens = ExpressionDiagnostics.tokens(expr);
        String ast = ExpressionDiagnostics.ast(expr);

        assertTrue(tokens.contains("LEFT_PARENTHESIS"));
        assertTrue(ast.contains("Binary(GREATER_OR_EQUAL)"));
    }
}
