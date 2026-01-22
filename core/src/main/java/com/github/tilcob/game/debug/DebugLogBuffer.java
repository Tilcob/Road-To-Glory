package com.github.tilcob.game.debug;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationLogger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class DebugLogBuffer implements ApplicationLogger {
    private static DebugLogBuffer active;

    private final ApplicationLogger delegate;
    private final Deque<String> lines;
    private final int maxLines;

    private DebugLogBuffer(ApplicationLogger delegate, int maxLines) {
        this.delegate = delegate;
        this.maxLines = Math.max(1, maxLines);
        this.lines = new ArrayDeque<>(this.maxLines);
    }

    public static void install(Application application, int maxLines) {
        if (application == null) return;
        DebugLogBuffer buffer = new DebugLogBuffer(application.getApplicationLogger(), maxLines);
        application.setApplicationLogger(buffer);
        active = buffer;
    }

    public static DebugLogBuffer getActive() {
        return active;
    }

    public List<String> getLines() {
        return new ArrayList<>(lines);
    }

    @Override
    public void log(String tag, String message) {
        addLine("INFO", tag, message);
        if (delegate != null) delegate.log(tag, message);
    }

    @Override
    public void log(String tag, String message, Throwable exception) {
        addLine("INFO", tag, message, exception);
        if (delegate != null) delegate.log(tag, message, exception);
    }

    @Override
    public void error(String tag, String message) {
        addLine("ERROR", tag, message);
        if (delegate != null) delegate.error(tag, message);
    }

    @Override
    public void error(String tag, String message, Throwable exception) {
        addLine("ERROR", tag, message, exception);
        if (delegate != null) delegate.error(tag, message, exception);
    }

    @Override
    public void debug(String tag, String message) {
        addLine("DEBUG", tag, message);
        if (delegate != null) delegate.debug(tag, message);
    }

    @Override
    public void debug(String tag, String message, Throwable exception) {
        addLine("DEBUG", tag, message, exception);
        if (delegate != null) delegate.debug(tag, message, exception);
    }

    private void addLine(String level, String tag, String message) {
        addLine(level, tag, message, null);
    }

    private void addLine(String level, String tag, String message, Throwable exception) {
        StringBuilder builder = new StringBuilder();
        builder.append(level);
        if (tag != null && !tag.isBlank()) {
            builder.append("[").append(tag).append("]");
        }
        builder.append(" ").append(message == null ? "" : message);
        if (exception != null && exception.getMessage() != null) {
            builder.append(" (").append(exception.getMessage()).append(")");
        }
        pushLine(builder.toString());
    }

    private void pushLine(String line) {
        if (lines.size() >= maxLines) lines.removeFirst();
        lines.addLast(line);
    }
}
