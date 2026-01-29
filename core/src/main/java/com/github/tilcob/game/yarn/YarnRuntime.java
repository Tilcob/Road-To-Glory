package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.github.tilcob.game.flow.CommandCall;

import java.util.*;

public class YarnRuntime {

    public Optional<CommandCall> parseCommandLine(String line) {
        return parseCommandLine(line, CommandCall.SourcePos.unknown());
    }

    public Optional<CommandCall> parseCommandLine(String line, CommandCall.SourcePos sourcePos) {
        if (line == null) return Optional.empty();

        String trimmed = line.trim();
        if (!isCommandLine(trimmed)) return Optional.empty(); // FIX: use trimmed

        String inner = trimmed.substring(2, trimmed.length() - 2).trim();
        if (inner.isEmpty()) return Optional.empty();

        List<String> tokens = tokenize(inner);
        if (tokens.isEmpty()) return Optional.empty();

        String command = tokens.get(0);
        List<String> arguments = tokens.size() > 1 ? tokens.subList(1, tokens.size()) : List.of();

        return Optional.of(new CommandCall(command, arguments, Map.of(), sourcePos));
    }

    public boolean isCommandLine(String line) {
        if (line == null) return false;
        return line.startsWith("<<") && line.endsWith(">>");
    }

    public List<String> tokenize(String inner) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < inner.length(); i++) {
            char c = inner.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
                current.append(c);
                continue;
            }

            if (!inQuotes && Character.isWhitespace(c)) {
                if (!current.isEmpty()) {
                    tokens.add(stripQuotes(current.toString()));
                    current.setLength(0);
                }
                continue;
            }

            current.append(c);
        }

        if (!current.isEmpty()) tokens.add(stripQuotes(current.toString()));
        return tokens;
    }

    private String stripQuotes(String token) {
        if (token != null && token.length() >= 2 && token.startsWith("\"") && token.endsWith("\"")) {
            return token.substring(1, token.length() - 1);
        }
        return token;
    }
}
