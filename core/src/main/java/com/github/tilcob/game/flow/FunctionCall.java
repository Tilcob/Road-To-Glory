package com.github.tilcob.game.flow;

import java.util.List;
import java.util.Map;

public record FunctionCall(String function,
                           List<String> arguments,
                           Map<String, Object> namedArguments,
                           CommandCall.SourcePos source) {
    public FunctionCall {
        if (function == null || function.isBlank()) throw new IllegalArgumentException("function name is blank");
        arguments = (arguments == null) ? List.of() : List.copyOf(arguments);
        namedArguments = (namedArguments == null) ? Map.of() : Map.copyOf(namedArguments);
        source = (source == null) ? CommandCall.SourcePos.unknown() : source;
    }

    public FunctionCall simple(String function, List<String> arguments, CommandCall.SourcePos source) {
        return new FunctionCall(function, arguments, Map.of(), source);
    }
}
