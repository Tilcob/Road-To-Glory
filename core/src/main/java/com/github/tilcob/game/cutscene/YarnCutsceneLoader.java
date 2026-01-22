package com.github.tilcob.game.cutscene;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.*;

public class YarnCutsceneLoader {
    private static final String HEADER_SEPARATOR = "---";
    private static final String NODE_SEPARATOR = "===";

    public CutsceneData load(String cutsceneId, FileHandle fileHandle) {
        if (fileHandle == null || !fileHandle.exists()) {
            Gdx.app.error("YarnCutsceneLoader", "Cutscene file missing: "
                + (fileHandle == null ? "null" : fileHandle.path()));
            return CutsceneData.empty(cutsceneId);
        }
        String content;
        try {
            content = fileHandle.readString("UTF-8");
        } catch (Exception e) {
            Gdx.app.error("YarnCutsceneLoader", "Failed to read cutscene file: " + fileHandle.path(), e);
            return CutsceneData.empty(cutsceneId);
        }

        List<ParsedNode> parsedNodes = parseNodes(content);
        ParsedNode startNode = findStartNode(parsedNodes);
        Array<String> startLines = startNode == null ? new Array<>() : toGdxArray(startNode.lines);

        Array<CutsceneNode> nodes = new Array<>();
        for (ParsedNode parsedNode : parsedNodes) {
            nodes.add(new CutsceneNode(parsedNode.id, toGdxArray(parsedNode.lines)));
        }

        ObjectMap<String, CutsceneNode> nodesById = new ObjectMap<>();
        for (CutsceneNode node : nodes) {
            if (node != null && node.id() != null) {
                nodesById.put(node.id(), node);
            }
        }

        return new CutsceneData(cutsceneId, startLines, nodesById);
    }

    private static ParsedNode findStartNode(List<ParsedNode> parsedNodes) {
        for (ParsedNode parsedNode : parsedNodes) {
            if (parsedNode.tags.contains("root") || parsedNode.tags.contains("start")) {
                return parsedNode;
            }
        }
        for (ParsedNode parsedNode : parsedNodes) {
            if ("start".equalsIgnoreCase(parsedNode.id)) {
                return parsedNode;
            }
        }
        return parsedNodes.isEmpty() ? null : parsedNodes.get(0);
    }

    private static List<ParsedNode> parseNodes(String content) {
        String[] lines = content.split("\\R", -1);
        List<ParsedNode> nodes = new ArrayList<>();
        List<String> headerLines = new ArrayList<>();
        List<String> bodyLines = new ArrayList<>();
        boolean inBody = false;

        for (String line : lines) {
            String trimmed = line.trim();
            if (NODE_SEPARATOR.equals(trimmed)) {
                ParsedNode node = buildNode(headerLines, bodyLines);
                if (node != null) {
                    nodes.add(node);
                }
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
                bodyLines.add(line);
            } else if (!trimmed.isEmpty()) {
                headerLines.add(line);
            }
        }

        ParsedNode node = buildNode(headerLines, bodyLines);
        if (node != null) {
            nodes.add(node);
        }
        return nodes;
    }

    private static ParsedNode buildNode(List<String> headerLines, List<String> bodyLines) {
        String title = null;
        Set<String> tags = new HashSet<>();

        for (String headerLine : headerLines) {
            String trimmed = headerLine.trim();
            String lower = trimmed.toLowerCase(Locale.ROOT);

            if (lower.startsWith("title:")) {
                title = trimmed.substring("title:".length()).trim();
            } else if (lower.startsWith("tags:")) {
                String tagValue = trimmed.substring("tags:".length()).trim();
                if (!tagValue.isEmpty()) {
                    for (String tag : tagValue.split("[,\\s]+")) {
                        if (!tag.isEmpty()) {
                            tags.add(tag.toLowerCase(Locale.ROOT));
                        }
                    }
                }
            }
        }

        if (title == null || title.isBlank()) {
            return null;
        }

        List<String> lines = new ArrayList<>();
        for (String line : bodyLines) {
            if (line == null) continue;
            lines.add(line.trim());
        }

        return new ParsedNode(title.trim(), tags, lines);
    }

    private static Array<String> toGdxArray(List<String> lines) {
        Array<String> array = new Array<>();
        if (lines == null) return array;
        for (String line : lines) {
            if (line == null) continue;
            array.add(line);
        }
        return array;
    }

    private record ParsedNode(String id, Set<String> tags, List<String> lines) {
    }
}
