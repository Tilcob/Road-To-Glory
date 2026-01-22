package com.github.tilcob.game.yarn;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;

import java.util.HashMap;
import java.util.Map;

public class YarnRuntime implements YarnCommandRegistry, YarnFunctionRegistry {
    private static final String TAG = YarnRuntime.class.getSimpleName();

    private final Map<String, CommandHandler> commandHandlers = new HashMap<>();
    private final Map<String, FunctionHandler> functionHandlers = new HashMap<>();

    @Override
    public void register(String command, CommandHandler handler) {
        if (command == null || command.isBlank() || handler == null) return;
        commandHandlers.put(command, handler);
    }

    @Override
    public void register(String function, FunctionHandler handler) {
        if (function == null || function.isBlank() || handler == null) return;
        functionHandlers.put(function, handler);
    }

    public boolean executeCommandLine(Entity player, String line) {
        if (line == null) return false;
        String trimmed = line.trim();
        if (!isCommandLine(trimmed)) return false;
        String inner = trimmed.substring(2, trimmed.length() - 2).trim();
        if (inner.isEmpty()) return true;

        String[] parts = inner.split("\\s+");
        String command = parts[0];
        String[] args = argsFrom(parts);
        boolean executed = executeCommand(command, player, args);

        if (!executed && Gdx.app != null) {
            Gdx.app.debug(TAG, "Unhandled Yarn command: " + command);
        }
        return true;
    }

    public boolean executeCommand(String command, Entity player, String[] args) {
        if (command == null) return false;
        CommandHandler handler = commandHandlers.get(command);
        if (handler == null) return false;
        handler.handle(player, args == null ? new String[0] : args);
        return true;
    }

    public Object evaluateFunction(String function, Entity player, String[] args) {
        if (function == null) return null;
        FunctionHandler handler = functionHandlers.get(function);
        if (handler == null) return null;
        return handler.evaluate(player, args == null ? new String[0] : args);
    }

    public static boolean isCommandLine(String line) {
        return line.startsWith("<<") && line.endsWith(">>");
    }

    private static String[] argsFrom(String[] parts) {
        if (parts.length <= 1) return new String[0];
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);
        return args;
    }
}
