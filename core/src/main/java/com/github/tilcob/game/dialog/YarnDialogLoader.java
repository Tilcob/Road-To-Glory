package com.github.tilcob.game.dialog;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.github.tilcob.game.event.QuestStepEvent;

import java.util.*;

public class YarnDialogLoader {
    private static final String HEADER_SEPARATOR = "---";
    private static final String NODE_SEPARATOR = "===";

    public DialogData load(FileHandle fileHandle) {
        List<ParsedNode> parsedNodes = parseNodes(fileHandle.readString("UTF-8"));
        ParsedNode rootNode = findTaggedNode(parsedNodes, "root", "start");
        ParsedNode idleNode = findTaggedNode(parsedNodes, "idle");
        Array<String> idleLines = idleNode == null ? new Array<>() : toGdxArray(idleNode.lines);
        Array<DialogChoice> rootChoices = rootNode == null ? new Array<>() : toChoiceArray(rootNode.choices);
        Array<DialogNode> nodes = new Array<>();
        for (ParsedNode parsedNode : parsedNodes) {
            if (parsedNode == rootNode || parsedNode == idleNode) {
                continue;
            }
            nodes.add(new DialogNode(parsedNode.id, toGdxArray(parsedNode.lines), toChoiceArray(parsedNode.choices)));
        }
        return new DialogData(idleLines, rootChoices, null, null, nodes);
    }

    private static ParsedNode findTaggedNode(List<ParsedNode> parsedNodes, String... tags) {
        for (String tag : tags) {
            String normalized = tag.toLowerCase(Locale.ROOT);
            for (ParsedNode parsedNode : parsedNodes) {
                if (parsedNode.tags.contains(normalized)) {
                    return parsedNode;
                }
            }
        }
        for (ParsedNode parsedNode : parsedNodes) {
            if ("start".equalsIgnoreCase(parsedNode.id)) {
                return parsedNode;
            }
        }
        return null;
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
            } else {
                if (!trimmed.isEmpty()) {
                    headerLines.add(line);
                }
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
            if (trimmed.toLowerCase(Locale.ROOT).startsWith("title:")) {
                title = trimmed.substring("title:".length()).trim();
            } else if (trimmed.toLowerCase(Locale.ROOT).startsWith("tags:")) {
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
        if (title == null || title.isEmpty()) {
            return null;
        }
        ParsedNode parsedNode = new ParsedNode(title, tags);
        parseBody(bodyLines, parsedNode);
        return parsedNode;
    }

    private static void parseBody(List<String> bodyLines, ParsedNode parsedNode) {
        ChoiceBuilder currentChoice = null;
        for (String rawLine : bodyLines) {
            String trimmed = rawLine.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.startsWith("->")) {
                if (currentChoice != null) {
                    parsedNode.choices.add(currentChoice);
                }
                currentChoice = new ChoiceBuilder(trimmed.substring(2).trim());
                continue;
            }
            if (currentChoice != null && isIndented(rawLine)) {
                if (tryApplyJump(trimmed, currentChoice)) {
                    continue;
                }
                if (tryApplyEffect(trimmed, currentChoice)) {
                    continue;
                }
                if (!isCommand(trimmed)) {
                    currentChoice.lines.add(trimmed);
                }
                continue;
            }
            if (currentChoice != null) {
                parsedNode.choices.add(currentChoice);
                currentChoice = null;
            }
            if (!isCommand(trimmed)) {
                parsedNode.lines.add(trimmed);
            }
        }
        if (currentChoice != null) {
            parsedNode.choices.add(currentChoice);
        }
    }

    private static boolean tryApplyJump(String line, ChoiceBuilder builder) {
        String normalized = line.replaceAll("\\s+", " ").trim();
        if (!normalized.startsWith("<<") || !normalized.endsWith(">>")) {
            return false;
        }
        String inner = normalized.substring(2, normalized.length() - 2).trim();
        if (inner.toLowerCase(Locale.ROOT).startsWith("jump ")) {
            builder.next = inner.substring("jump ".length()).trim();
            return true;
        }
        if (inner.toLowerCase(Locale.ROOT).startsWith("goto ")) {
            builder.next = inner.substring("goto ".length()).trim();
            return true;
        }
        return false;
    }

    private static boolean tryApplyEffect(String line, ChoiceBuilder builder) {
        if (!isCommand(line)) {
            return false;
        }
        String inner = line.substring(2, line.length() - 2).trim();
        if (inner.isEmpty()) {
            return false;
        }
        String[] parts = inner.split("\\s+");
        String command = parts[0].toLowerCase(Locale.ROOT);
        switch (command) {
            case "set_flag" -> {
                if (parts.length < 3) {
                    return false;
                }
                String flag = parts[1];
                Boolean value = Boolean.parseBoolean(parts[2]);
                builder.effects.add(DialogEffect.setFlag(flag, value));
                return true;
            }
            case "add_quest" -> {
                if (parts.length < 2) {
                    return false;
                }
                builder.effects.add(DialogEffect.addQuest(parts[1]));
                return true;
            }
            case "quest_step" -> {
                if (parts.length < 3) {
                    return false;
                }
                QuestStepEvent.Type stepType = parseQuestStepType(parts[1]);
                if (stepType == null) {
                    return false;
                }
                builder.effects.add(DialogEffect.questStep(stepType, parts[2]));
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private static boolean isCommand(String line) {
        return line.startsWith("<<") && line.endsWith(">>");
    }

    private static boolean isIndented(String line) {
        return !line.isEmpty() && Character.isWhitespace(line.charAt(0));
    }

    private static Array<String> toGdxArray(List<String> lines) {
        Array<String> array = new Array<>();
        for (String line : lines) {
            array.add(line);
        }
        return array;
    }

    private static Array<DialogEffect> toGdxEffectArray(List<DialogEffect> effects) {
        Array<DialogEffect> array = new Array<>();
        for (DialogEffect effect : effects) {
            array.add(effect);
        }
        return array;
    }

    private static Array<DialogChoice> toChoiceArray(List<ChoiceBuilder> choices) {
        Array<DialogChoice> array = new Array<>();
        for (ChoiceBuilder choice : choices) {
            array.add(choice.toDialogChoice());
        }
        return array;
    }

    private static QuestStepEvent.Type parseQuestStepType(String raw) {
        for (QuestStepEvent.Type type : QuestStepEvent.Type.values()) {
            if (type.name().equalsIgnoreCase(raw)) {
                return type;
            }
        }
        return null;
    }

    private static final class ParsedNode {
        private final String id;
        private final Set<String> tags;
        private final List<String> lines = new ArrayList<>();
        private final List<ChoiceBuilder> choices = new ArrayList<>();

        private ParsedNode(String id, Set<String> tags) {
            this.id = id;
            this.tags = tags;
        }
    }

    private static final class ChoiceBuilder {
        private final String text;
        private final List<String> lines = new ArrayList<>();
        private final List<DialogEffect> effects = new ArrayList<>();
        private String next;

        private ChoiceBuilder(String text) {
            this.text = text;
        }

        private DialogChoice toDialogChoice() {
            Array<DialogEffect> effectArray = effects.isEmpty() ? null : toGdxEffectArray(effects);
            return new DialogChoice(text, toGdxArray(lines), effectArray, next);
        }
    }
}
