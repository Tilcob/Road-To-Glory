package com.github.tilcob.game.yarn.script;

import java.util.ArrayList;
import java.util.List;

public final class ScriptComplier {

    private ScriptComplier() {}

    public static List<ScriptEvent> compile(List<String> bodyLines) {
        List<ScriptEvent> events = new ArrayList<>();
        if (bodyLines == null) return events;

        for (String line : bodyLines) {
            if (line == null) continue;
            String trimmed = line.trim();

            if (trimmed.startsWith("<<if ") && trimmed.endsWith(">>")) {
                String cond = trimmed.substring(2, trimmed.length() - 2).trim()
                    .substring("if ".length()).trim();
                events.add(new ScriptEvent.IfStart(cond));
                continue;
            }

            if (trimmed.equals("<<else>>")) {
                events.add(new ScriptEvent.Else());
                continue;
            }

            if (trimmed.equals("<<endif>>")) {
                events.add(new ScriptEvent.EndIf());
                continue;
            }

            if (trimmed.startsWith("<<") && trimmed.endsWith(">>")) {
                events.add(new ScriptEvent.Command(trimmed));
                continue;
            }

            events.add(new ScriptEvent.Text(line));
        }

        return events;
    }
}
