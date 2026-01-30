package com.github.tilcob.game.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.github.tilcob.game.event.QuestStepEvent;
import com.github.tilcob.game.yarn.YarnParser;
import com.github.tilcob.game.yarn.script.ScriptComplier;
import com.github.tilcob.game.yarn.script.ScriptEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class YarnDialogLoader {

    public DialogData load(FileHandle fileHandle) {
        if (fileHandle == null || !fileHandle.exists()) {
            Gdx.app.error("YarnDialogLoader", "Dialog file missing: " + (fileHandle == null ? "null" : fileHandle.path()));
            return DialogData.empty();
        }
        try {
            fileHandle.readString("UTF-8");
        } catch (Exception e) {
            Gdx.app.error("YarnDialogLoader", "Failed to read dialog file: " + fileHandle.path(), e);
            return DialogData.empty();
        }

        List<YarnParser.YarnNodeRaw> rawNodes = YarnParser.parse(fileHandle.readString("UTF-8"));

        List<ParsedNode> parsedNodes = new ArrayList<>();
        for (YarnParser.YarnNodeRaw raw : rawNodes) {
            ParsedNode p = new ParsedNode(raw.id(), raw.tags());
            parseBody(raw.bodyLines(), p);
            p.events = ScriptComplier.compile(p.lines);
            parsedNodes.add(p);
        }

        ParsedNode rootNode = findTaggedNode(parsedNodes, "root", "start");
        ParsedNode idleNode = findTaggedNode(parsedNodes, "idle");

        Array<ScriptEvent> idleEvents = idleNode == null ? new Array<>() : toGdxEvents(idleNode.events);
        Array<ScriptEvent> rootEvents = rootNode == null ? new Array<>() : toGdxEvents(rootNode.events);
        Array<DialogChoice> rootChoices = rootNode == null ? new Array<>() : toChoiceArray(rootNode.choices);

        Array<DialogNode> nodes = new Array<>();
        Array<DialogFlagDialog> flagDialogs = new Array<>();

        QuestDialog questDialog = null;
        String questId = null;
        Array<ScriptEvent> questNotStarted = null;
        Array<ScriptEvent> questInProgress = null;
        Array<ScriptEvent> questCompleted = null;
        Array<DialogChoice> questNotStartedChoices = null;
        Array<DialogChoice> questInProgressChoices = null;
        Array<DialogChoice> questCompletedChoices = null;
        ObjectMap<String, Array<ScriptEvent>> stepDialogs = new ObjectMap<>();
        ObjectMap<String, Array<DialogChoice>> stepChoices = new ObjectMap<>();

        for (ParsedNode parsedNode : parsedNodes) {
            if (parsedNode == rootNode) continue;

            // --- QUEST NODES: collect quest scriptEvents AND keep as normal nodes so <<jump quest_...>> works ---
            String questTag = findQuestTag(parsedNode.tags);
            if (questTag != null) {
                // questId from tag "quest_<id>"
                questId = questTag;

                Array<ScriptEvent> questEvents = toGdxEvents(parsedNode.events);
                Array<DialogChoice> questChoices = toChoiceArray(parsedNode.choices);

                if (parsedNode.tags.contains("notstarted")) {
                    questNotStarted = questEvents;
                    questNotStartedChoices = questChoices;
                } else if (parsedNode.tags.contains("inprogress")) {
                    questInProgress = questEvents;
                    questInProgressChoices = questChoices;
                } else if (parsedNode.tags.contains("completed")) {
                    questCompleted = questEvents;
                    questCompletedChoices = questChoices;
                } else {
                    String stepIndex = findStepIndex(parsedNode.tags);
                    if (stepIndex != null) {
                        stepDialogs.put(stepIndex, questEvents);
                        stepChoices.put(stepIndex, questChoices);
                    }
                }

                // IMPORTANT: ALSO store it as a normal DialogNode so jump/goto can target it
                nodes.add(new DialogNode(parsedNode.id, questEvents, toChoiceArray(parsedNode.choices)));
                continue;
            }

            // --- FLAG DIALOGS ---
            String flagTag = findFlagTag(parsedNode.tags);
            if (flagTag != null) {
                flagDialogs.add(new DialogFlagDialog(flagTag, toGdxEvents(parsedNode.events)));
                continue;
            }

            // --- NORMAL NODES ---
            nodes.add(new DialogNode(parsedNode.id, toGdxEvents(parsedNode.events), toChoiceArray(parsedNode.choices)));
        }

        if (questId != null) {
            ObjectMap<String, Array<ScriptEvent>> stepDialogMap = stepDialogs.size == 0 ? null : stepDialogs;
            ObjectMap<String, Array<DialogChoice>> stepChoiceMap = stepChoices.size == 0 ? null : stepChoices;
            questDialog = new QuestDialog(
                questId,
                defaultEvents(questNotStarted),
                defaultEvents(questInProgress),
                defaultEvents(questCompleted),
                stepDialogMap,
                defaultChoices(questNotStartedChoices),
                defaultChoices(questInProgressChoices),
                defaultChoices(questCompletedChoices),
                stepChoiceMap
            );
        }

        Array<DialogFlagDialog> flagDialogArray = flagDialogs.size == 0 ? null : flagDialogs;
        ObjectMap<String, DialogNode> nodesById = buildNodeMap(nodes);

        return new DialogData(idleEvents, rootEvents, rootChoices, flagDialogArray, questDialog, nodes, nodesById);
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
                currentChoice.lines.add(trimmed);
                continue;
            }

            if (currentChoice != null) {
                parsedNode.choices.add(currentChoice);
                currentChoice = null;
            }
            parsedNode.lines.add(trimmed);
        }

        if (currentChoice != null) {
            parsedNode.choices.add(currentChoice);
        }
        parsedNode.events = ScriptComplier.compile(parsedNode.lines);
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

    private static Array<ScriptEvent> toGdxEvents(List<ScriptEvent> events) {
        Array<ScriptEvent> out = new Array<>();
        if (events == null) return out;
        for (ScriptEvent e : events) out.add(e);
        return out;
    }

    private static Array<ScriptEvent> defaultEvents(Array<ScriptEvent> events) {
        return events == null ? new Array<>() : events;
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
        private List<ScriptEvent> events = List.of();

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
            return new DialogChoice(text, toGdxEvents(ScriptComplier.compile(lines)), effectArray, next);
        }
    }
}
