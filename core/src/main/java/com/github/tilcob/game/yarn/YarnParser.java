package com.github.tilcob.game.yarn;

import java.util.*;

public class YarnParser {
    private static final String HEADER_SEPARATOR = "---";
    private static final String NODE_SEPARATOR = "===";

    private YarnParser() {}

    public static List<YarnNodeRaw> parse(String content) {
        if (content == null) return List.of();

        String[] lines = content.split("\\R", -1);
        List<YarnNodeRaw> nodes = new ArrayList<>();
        List<String> headerLines = new ArrayList<>();
        List<String> bodyLines = new ArrayList<>();
        boolean inBody = false;

        for (String line : lines) {
            String trimmed = line == null ? "" : line.trim();

            if (NODE_SEPARATOR.equals(trimmed)) {
                YarnNodeRaw node = buildNode(headerLines, bodyLines);
                if (node != null) nodes.add(node);

                headerLines.clear();
                bodyLines.clear();
                inBody = false;
                continue;
            }

            if (!inBody && HEADER_SEPARATOR.equals(trimmed)) {
                inBody = true;
                continue;
            }

            if (inBody) {
                // IMPORTANT: raw behalten (Dialog braucht Indents)
                bodyLines.add(line == null ? "" : line);
            } else {
                if (!trimmed.isEmpty()) {
                    headerLines.add(line);
                }
            }
        }

        YarnNodeRaw last = buildNode(headerLines, bodyLines);
        if (last != null) nodes.add(last);

        return nodes;
    }

    private static YarnNodeRaw buildNode(List<String> headerLines, List<String> bodyLines) {
        String title = null;
        Set<String> tags = new HashSet<>();

        for (String headerLine : headerLines) {
            String trimmed = headerLine == null ? "" : headerLine.trim();
            String lower = trimmed.toLowerCase(Locale.ROOT);

            if (lower.startsWith("title:")) {
                title = trimmed.substring("title:".length()).trim();
            } else if (lower.startsWith("tags:")) {
                String tagValue = trimmed.substring("tags:".length()).trim();
                if (!tagValue.isEmpty()) {
                    for (String tag : tagValue.split("[,\\s]+")) {
                        if (!tag.isEmpty()) tags.add(tag.toLowerCase(Locale.ROOT));
                    }
                }
            }
        }

        if (title == null || title.isBlank()) return null;

        return new YarnNodeRaw(title.trim(), tags, List.copyOf(bodyLines));
    }

    public record YarnNodeRaw(String id, Set<String> tags, List<String> bodyLines) {}
}
