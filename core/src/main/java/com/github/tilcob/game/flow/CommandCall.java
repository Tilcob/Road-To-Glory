package com.github.tilcob.game.flow;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record CommandCall(
    String command,
    List<String> arguments,
    Map<String, Objects> namedArguments,
    SourcePos source
) {
    public CommandCall {
        if (command == null || command.isBlank()) throw new IllegalArgumentException("Command command must be non-empty.");
        arguments = arguments == null ? List.of() : List.copyOf(arguments);
        namedArguments = namedArguments == null ? Map.of() : Map.copyOf(namedArguments);
    }

    public static CommandCall simple(String name, List<String> arguments) {
        return new CommandCall(name, arguments, Map.of(), SourcePos.unknown());
    }

    public record SourcePos(String origin, String node, int line) {
        public static SourcePos unknown() { return new SourcePos("unknown", "unknown", -1); }
    }
}
