package com.github.tilcob.game.yarn.expression;

import com.github.tilcob.game.flow.CommandCall;

import java.util.List;
import java.util.stream.Collectors;

public final class ExpressionDiagnostics {
    private ExpressionDiagnostics() {}

    public static String tokens(String expr) {
        List<ExpressionToken> t = new ExpressionLexer(expr, CommandCall.SourcePos.unknown()).lex();
        return t.stream()
            .map(tok -> tok.type() + "(" + tok.value() + ")@" + tok.pos())
            .collect(Collectors.joining("\n"));
    }

    public static String ast(String expr) {
        List<ExpressionToken> t = new ExpressionLexer(expr, CommandCall.SourcePos.unknown()).lex();
        ExpressionParser.Node n = new ExpressionParser(t, expr, CommandCall.SourcePos.unknown()).parse();
        return dump(n, 0);
    }

    private static String dump(ExpressionParser.Node n, int indent) {
        String pad = "  ".repeat(Math.max(0, indent));

        if (n instanceof ExpressionParser.Node.Literal lit) {
            return pad + "Literal(" + lit.value() + ")";
        }
        if (n instanceof ExpressionParser.Node.Var v) {
            return pad + "Var(" + v.name() + ")";
        }
        if (n instanceof ExpressionParser.Node.Call c) {
            StringBuilder sb = new StringBuilder();
            sb.append(pad).append("Call(").append(c.name()).append(")\n");
            for (ExpressionParser.Node a : c.args()) {
                sb.append(dump(a, indent + 1)).append("\n");
            }
            return sb.toString().trim();
        }
        if (n instanceof ExpressionParser.Node.Unary u) {
            return pad + "Unary(" + u.type() + ")\n" + dump(u.right(), indent + 1);
        }
        if (n instanceof ExpressionParser.Node.Binary b) {
            return pad + "Binary(" + b.type() + ")\n"
                + dump(b.left(), indent + 1) + "\n"
                + dump(b.right(), indent + 1);
        }
        return pad + "Unknown(" + n + ")";
    }
}
