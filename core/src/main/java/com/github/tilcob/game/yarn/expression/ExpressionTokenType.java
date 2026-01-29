package com.github.tilcob.game.yarn.expression;

public enum ExpressionTokenType {
    LEFT_PARENTHESIS, RIGHT_PARENTHESIS, COMMA,

    // operators
    NOT,            // !
    AND,         // &&
    OR,           // ||
    EQUAL,     // ==
    NOT_EQUAL,      // !=
    GREATER,         // >
    GREATER_OR_EQUAL,   // >=
    LESS,            // <
    LESS_OR_EQUAL,      // <=

    // literals / identifiers
    IDENT,
    STRING,
    NUMBER,
    TRUE,
    FALSE,

    EOF
}
