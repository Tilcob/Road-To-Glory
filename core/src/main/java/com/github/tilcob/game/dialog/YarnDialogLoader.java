package com.github.tilcob.game.dialog;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
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
        Array<String> rootLines = rootNode == null ? new Array<>() : toGdxArray(rootNode.lines);
        Array<DialogChoice> rootChoices = rootNode == null ? new Array<>() : toChoiceArray(rootNode.choices);

        Array<DialogNode> nodes = new Array<>();
        Array<DialogFlagDialog> flagDialogs = new Array<>();

        QuestDialog questDialog = null;
        String questId = null;
        Array<String> questNotStarted = null;
        Array<String> questInProgress = null;
        Array<String> questCompleted = null;
        Array<DialogChoice> questNotStartedChoices = null;
        Array<DialogChoice> questInProgressChoices = null;
        Array<DialogChoice> questCompletedChoices = null;
        ObjectMap<String, Array<String>> stepDialogs = new ObjectMap<>();
        ObjectMap<String, Array<DialogChoice>> stepChoices = new ObjectMap<>();

        for (ParsedNode parsedNode : parsedNodes) {
            if (parsedNode == rootNode) continue;

            // --- QUEST NODES: collect quest lines AND keep as normal nodes so <<jump quest_...>> works ---
            String questTag = findQuestTag(parsedNode.tags);
            if (questTag != null) {
                // questId from tag "quest_<id>"
                questId = questTag;

                Array<String> questLines = toGdxArray(parsedNode.lines);
                Array<DialogChoice> questChoices = toChoiceArray(parsedNode.choices);

                if (parsedNode.tags.contains("notstarted")) {
                    questNotStarted = questLines;
                    questNotStartedChoices = questChoices;
                } else if (parsedNode.tags.contains("inProgress")) {
                    questInProgress = questLines;
                    questInProgressChoices = questChoices;
                } else if (parsedNode.tags.contains("completed")) {
                    questCompleted = questLines;
                    questCompletedChoices = questChoices;
                } else {
                    String stepIndex = findStepIndex(parsedNode.tags);
                    if (stepIndex != null) {
                        stepDialogs.put(stepIndex, questLines);
                        stepChoices.put(stepIndex, questChoices);
                    }
                }

                // IMPORTANT: ALSO store it as a normal DialogNode so jump/goto can target it
                nodes.add(new DialogNode(parsedNode.id, questLines, toChoiceArray(parsedNode.choices)));
                continue;
            }

            // --- FLAG DIALOGS ---
            String flagTag = findFlagTag(parsedNode.tags);
            if (flagTag != null) {
                flagDialogs.add(new DialogFlagDialog(flagTag, toGdxArray(parsedNode.lines)));
                continue;
            }

            // --- NORMAL NODES ---
            nodes.add(new DialogNode(parsedNode.id, toGdxArray(parsedNode.lines), toChoiceArray(parsedNode.choices)));
        }

        if (questId != null) {
            ObjectMap<String, Array<String>> stepDialogMap = stepDialogs.size == 0 ? null : stepDialogs;
            ObjectMap<String, Array<DialogChoice>> stepChoiceMap = stepChoices.size == 0 ? null : stepChoices;
            questDialog = new QuestDialog(
                questId,
                defaultLines(questNotStarted),
                defaultLines(questInProgress),
                defaultLines(questCompleted),
                stepDialogMap,
                defaultChoices(questNotStartedChoices),
                defaultChoices(questInProgressChoices),
                defaultChoices(questCompletedChoices),
                stepChoiceMap
            );
        }

        Array<DialogFlagDialog> flagDialogArray = flagDialogs.size == 0 ? null : flagDialogs;
        ObjectMap<String, DialogNode> nodesById = buildNodeMap(nodes);

        return new DialogData(idleLines, rootLines, rootChoices, flagDialogArray, questDialog, nodes, nodesById);
    }

    private ObjectMap<String, DialogNode> buildNodeMap(Array<DialogNode> nodes) {
        ObjectMap<String, DialogNode> nodesById = new ObjectMap<>();
        if (nodes == null || nodes.isEmpty()) return nodesById;
        for (DialogNode node : nodes) {
            if (node == null || node.id() == null) continue;
            nodesById.put(node.id(), node);
        }
        return nodesById;
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
                if (isCommand(trimmed)) {
                    currentChoice.lines.add(trimmed);
                }
                continue;
            }

            if (currentChoice != null) {
                parsedNode.choices.add(currentChoice);
                currentChoice = null;
            }

            if (isCommand(trimmed)) {
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
        String lower = inner.toLowerCase(Locale.ROOT);

        if (lower.startsWith("jump ")) {
            builder.next = inner.substring("jump ".length()).trim();
            return true;
        }
        if (lower.startsWith("goto ")) {
            builder.next = inner.substring("goto ".length()).trim();
            return true;
        }
        return false;
    }

    private static boolean tryApplyEffect(String line, ChoiceBuilder builder) {
        if (isCommand(line)) {
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
                boolean value = Boolean.parseBoolean(parts[2]);
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
        return !line.startsWith("<<") || !line.endsWith(">>");
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

    private static Array<String> defaultLines(Array<String> lines) {
        return lines == null ? new Array<>() : lines;
    }

    private static Array<DialogChoice> defaultChoices(Array<DialogChoice> choices) {
        return choices == null ? new Array<>() : choices;
    }

    private static String findQuestTag(Set<String> tags) {
        for (String tag : tags) {
            if (tag.startsWith("quest_") && tag.length() > "quest_".length()) {
                return tag.substring("quest_".length());
            }
        }
        return null;
    }

    private static String findFlagTag(Set<String> tags) {
        for (String tag : tags) {
            if (tag.startsWith("flag_") && tag.length() > "flag_".length()) {
                return tag.substring("flag_".length());
            }
        }
        return null;
    }

    private static String findStepIndex(Set<String> tags) {
        for (String tag : tags) {
            if (tag.startsWith("step_") && tag.length() > "step_".length()) {
                return tag.substring("step_".length());
            }
        }
        return null;
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
