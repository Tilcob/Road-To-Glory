package com.github.tilcob.game.yarn.expression;

import com.github.tilcob.game.flow.CommandCall;

public class YarnExpressionException extends RuntimeException {
    private final CommandCall.SourcePos source;
    private final String expression;
    private final int expressionPos;

    public YarnExpressionException(CommandCall.SourcePos source, String expression,
                                   int expressionPos, String message) {
        super(message);
        this.source = source;
        this.expression = expression == null ? "" : expression;
        this.expressionPos = expressionPos;
    }

    public CommandCall.SourcePos source() {
        return source;
    }

    public String expression() {
        return expression;
    }

    public int expressionPos() {
        return expressionPos;
    }
}
