package com.github.tilcob.game;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class QuestContentValidator {
    private static final Pattern HEADER_PATTERN = Pattern.compile("^([A-Za-z0-9_.-]+)\\s*:\\s*(.*)$");
    private static final Pattern TITLE_PATTERN = Pattern.compile("^title\\s*:\\s*(.+)$");
    private static final Pattern TAGS_PATTERN = Pattern.compile("^tags\\s*:\\s*(.+)$");
    private static final String HEADER_TERMINATOR = "---";
    private static final Set<String> VALID_REWARD_TIMINGS = Set.of("GIVER", "COMPLETION");

    private final Path assetsRoot;
    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    public QuestContentValidator(Path assetsRoot) {
        this.assetsRoot = assetsRoot;
    }

    public static void main(String[] args) {
        Path assetsRoot = args.length > 0 ? Path.of(args[0]) : Path.of("assets");
        QuestContentValidator validator = new QuestContentValidator(assetsRoot);
        int exitCode = validator.run();
        System.exit(exitCode);
    }

    public int run() {
        Set<String> questIds = validateQuestIndex();
        validateDialogTags(questIds);
        reportSummary();
        return errors.isEmpty() ? 0 : 1;
    }

    private Set<String> validateQuestIndex() {
        Set<String> questIds = new HashSet<>();
        Path indexPath = assetsRoot.resolve("quests").resolve("index.json");
        if (!Files.exists(indexPath)) {
            errors.add("Missing quest index: " + indexPath);
            return questIds;
        }
        JsonValue root;
        try {
            root = new JsonReader().parse(Files.readString(indexPath));
        } catch (IOException e) {
            errors.add("Failed to read quest index: " + indexPath + " (" + e.getMessage() + ")");
            return questIds;
        }
        if (!root.isArray()) {
            errors.add("Quest index must be a JSON array: " + indexPath);
            return questIds;
        }
        for (JsonValue entry = root.child; entry != null; entry = entry.next) {
            if (!entry.isString()) {
                errors.add("Invalid quest index entry (not a string): " + entry);
                continue;
            }
            String value = entry.asString();
            Path questFile = resolveQuestFile(value);
            questIds.addAll(validateQuestFile(value, questFile));
        }
        return questIds;
    }

    private Path resolveQuestFile(String entry) {
        String fileName = entry.toLowerCase(Locale.ROOT).endsWith(".yarn") ? entry : entry + ".yarn";
        if (fileName.contains("/")) {
            return assetsRoot.resolve(fileName);
        }
        return assetsRoot.resolve("quests").resolve(fileName);
    }

    private Set<String> validateQuestFile(String entry, Path questFile) {
        Set<String> questIds = new HashSet<>();
        if (!Files.exists(questFile)) {
            errors.add("Quest file not found for index entry '" + entry + "': " + questFile);
            return questIds;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(questFile);
        } catch (IOException e) {
            errors.add("Failed to read quest file: " + questFile + " (" + e.getMessage() + ")");
            return questIds;
        }
        List<String> headerLines = readHeaderLines(lines);
        if (headerLines.isEmpty()) {
            errors.add("Quest file missing headers: " + questFile);
            return questIds;
        }
        Map<String, List<String>> headers = parseHeaderLines(headerLines);
        String questId = getFirst(headers, "questId");
        String displayName = getFirst(headers, "displayName");
        String journalText = getFirst(headers, "journalText");
        String startNode = getFirst(headers, "startNode");
        if (questId == null || questId.isBlank()) {
            errors.add("Quest file missing questId: " + questFile);
        } else {
            questIds.add(questId);
        }
        if (displayName == null || displayName.isBlank()) {
            errors.add("Quest file missing displayName: " + questFile);
        }
        if (journalText == null || journalText.isBlank()) {
            errors.add("Quest file missing journalText: " + questFile);
        }
        if (startNode == null || startNode.isBlank()) {
            errors.add("Quest file missing startNode: " + questFile);
        }
        if (questId != null && !questId.isBlank()) {
            String fileStem = questFile.getFileName().toString().replace(".yarn", "");
            if (!fileStem.equals(questId)) {
                warnings.add("QuestId '" + questId + "' does not match file name '" + fileStem + "' in " + questFile);
            }
        }
        validateRewardTiming(headers, questFile);
        validateStepHeaders(headers, questFile);
        validateStartNodeExists(startNode, lines, questFile);
        return questIds;
    }

    private void validateRewardTiming(Map<String, List<String>> headers, Path questFile) {
        String timing = getFirst(headers, "reward_timing");
        if (timing == null || timing.isBlank()) {
            return;
        }
        String normalized = normalizeHeaderValue(timing).toUpperCase(Locale.ROOT);
        if (!VALID_REWARD_TIMINGS.contains(normalized)) {
            errors.add("Invalid reward_timing '" + timing + "' in " + questFile);
        }
    }

    private void validateStepHeaders(Map<String, List<String>> headers, Path questFile) {
        List<String> steps = headers.getOrDefault("step", List.of());
        for (String step : steps) {
            String[] parts = step.trim().split("\\s+");
            if (parts.length == 0) {
                errors.add("Empty step definition in " + questFile);
                continue;
            }
            String type = parts[0].toLowerCase(Locale.ROOT);
            switch (type) {
                case "talk" -> {
                    if (parts.length < 2) {
                        errors.add("Talk step missing npc in " + questFile);
                    }
                }
                case "collect", "kill" -> {
                    if (parts.length < 3) {
                        errors.add("Step missing args (" + type + ") in " + questFile);
                    } else if (!isInteger(parts[2])) {
                        errors.add("Step amount not a number (" + parts[2] + ") in " + questFile);
                    }
                }
                default -> errors.add("Unknown step type '" + parts[0] + "' in " + questFile);
            }
        }
    }

    private void validateStartNodeExists(String startNode, List<String> lines, Path questFile) {
        if (startNode == null || startNode.isBlank()) {
            return;
        }
        Set<String> nodeTitles = parseNodeTitles(lines);
        if (!nodeTitles.contains(startNode)) {
            errors.add("startNode '" + startNode + "' not found in " + questFile);
        }
    }

    private void validateDialogTags(Set<String> questIds) {
        Path dialogDir = assetsRoot.resolve("dialogs");
        if (!Files.isDirectory(dialogDir)) {
            warnings.add("Dialog directory not found: " + dialogDir);
            return;
        }
        try {
            Files.list(dialogDir)
                .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".yarn"))
                .forEach(path -> validateDialogFile(path, questIds));
        } catch (IOException e) {
            errors.add("Failed to read dialog directory: " + dialogDir + " (" + e.getMessage() + ")");
        }
    }

    private void validateDialogFile(Path dialogFile, Set<String> questIds) {
        List<String> lines;
        try {
            lines = Files.readAllLines(dialogFile);
        } catch (IOException e) {
            errors.add("Failed to read dialog file: " + dialogFile + " (" + e.getMessage() + ")");
            return;
        }
        for (String line : lines) {
            Matcher matcher = TAGS_PATTERN.matcher(line.trim());
            if (!matcher.matches()) {
                continue;
            }
            String[] tags = matcher.group(1).split(",");
            for (String tag : tags) {
                String trimmed = tag.trim();
                if (trimmed.startsWith("quest_")) {
                    String questId = trimmed.substring("quest_".length());
                    if (!questIds.contains(questId)) {
                        errors.add("Dialog tag references unknown quest '" + questId + "' in " + dialogFile);
                    }
                }
            }
        }
    }

    private Set<String> parseNodeTitles(List<String> lines) {
        Set<String> titles = new HashSet<>();
        for (String line : lines) {
            Matcher matcher = TITLE_PATTERN.matcher(line.trim());
            if (matcher.matches()) {
                titles.add(normalizeHeaderValue(matcher.group(1)));
            }
        }
        return titles;
    }

    private List<String> readHeaderLines(List<String> lines) {
        List<String> headers = new ArrayList<>();
        for (String line : lines) {
            if (line.trim().equals(HEADER_TERMINATOR)) {
                break;
            }
            if (!line.trim().isEmpty()) {
                headers.add(line);
            }
        }
        return headers;
    }

    private Map<String, List<String>> parseHeaderLines(List<String> headerLines) {
        Map<String, List<String>> headers = new HashMap<>();
        for (String line : headerLines) {
            Matcher matcher = HEADER_PATTERN.matcher(line);
            if (!matcher.matches()) {
                continue;
            }
            String key = matcher.group(1);
            String value = normalizeHeaderValue(matcher.group(2));
            if (!value.isBlank()) {
                headers.computeIfAbsent(key, ignored -> new ArrayList<>()).add(value);
            }
        }
        return headers;
    }

    private String getFirst(Map<String, List<String>> headers, String key) {
        List<String> values = headers.get(key);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    private String normalizeHeaderValue(String value) {
        String trimmed = value.trim();
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
            || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            return trimmed.substring(1, trimmed.length() - 1).trim();
        }
        return trimmed;
    }

    private boolean isInteger(String value) {
        try {
            Integer.parseInt(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void reportSummary() {
        if (!warnings.isEmpty()) {
            System.out.println("Warnings:");
            warnings.forEach(warning -> System.out.println("  - " + warning));
        }
        if (!errors.isEmpty()) {
            System.err.println("Errors:");
            errors.forEach(error -> System.err.println("  - " + error));
        }
        System.out.println("Quest validation complete. Errors: " + errors.size() + ", Warnings: " + warnings.size());
    }
}
