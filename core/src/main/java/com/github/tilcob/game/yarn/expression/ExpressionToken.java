package com.github.tilcob.game.yarn.expression;

public record ExpressionToken(ExpressionTokenType type, String value, int pos) {
    @Override
    public String toString() {
        return type + "(" + value + ")@" + pos;
    }
}
