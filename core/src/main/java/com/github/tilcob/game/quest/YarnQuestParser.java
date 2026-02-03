package com.github.tilcob.game.quest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YarnQuestParser {
    private static final String TAG = YarnQuestParser.class.getSimpleName();
    private static final String YARN_HEADER_TERMINATOR = "---";
    private static final Pattern HEADER_PATTERN = Pattern.compile("^([A-Za-z0-9_.-]+)\\s*:\\s*(.*)$");

    public QuestDefinition parse(FileHandle questFile) {
        List<String> headerLines = readYarnHeaders(questFile);
        if (headerLines.isEmpty()) {
            Gdx.app.error(TAG, "Quest file missing Yarn headers: " + questFile.path());
            return null;
        }

        Map<String, List<String>> headers = parseHeaderLines(headerLines);
        String questId = getFirst(headers, "questId");
        String displayName = getFirst(headers, "displayName");
        String journalText = getFirst(headers, "journalText");
        String startNode = getFirst(headers, "startNode");
        List<String> stepJournals = headers.getOrDefault("step_journal", List.of());
        List<QuestDefinition.StepDefinition> steps = parseSteps(
            headers.getOrDefault("step", List.of()),
            stepJournals,
            questFile
        );

        if (questId == null || questId.isBlank()) {
            Gdx.app.error(TAG, "Quest entry missing questId: " + questFile.path());
            return null;
        }
        if (startNode == null || startNode.isBlank()) {
            Gdx.app.error(TAG, "Quest entry missing startNode: " + questFile.path());
            return null;
        }
        if (displayName == null || displayName.isBlank()) {
            Gdx.app.error(TAG, "Missing displayName for quest: " + questId);
            return null;
        }
        if (journalText == null || journalText.isBlank()) {
            Gdx.app.error(TAG, "Missing journalText for quest: " + questId);
            return null;
        }

        int rewardMoney = parseRewardMoney(headers, questFile);
        int rewardExp = parseRewardExp(headers, questFile);
        List<String> rewardItems = parseRewardItems(headers);
        RewardTiming rewardTiming = parseRewardTiming(headers, questFile);
        QuestDefinition.RewardDefinition reward = new QuestDefinition.RewardDefinition(
            rewardMoney,
            rewardExp,
            List.copyOf(rewardItems)
        );

        return new QuestDefinition(
            questId,
            displayName,
            journalText,
            startNode,
            List.copyOf(steps),
            rewardTiming,
            reward
        );
    }

    private Map<String, List<String>> parseHeaderLines(List<String> headerLines) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        for (String line : headerLines) {
            Matcher matcher = HEADER_PATTERN.matcher(line);
            if (!matcher.matches()) {
                continue;
            }
            String key = matcher.group(1);
            String value = normalizeHeaderValue(matcher.group(2));
            if (key.equals("reward_items")) {
                addRewardItems(headers, value);
            } else if (!value.isBlank()) {
                headers.computeIfAbsent(key, ignored -> new ArrayList<>()).add(value);
            }
        }
        return headers;
    }

    private void addRewardItems(Map<String, List<String>> headers, String value) {
        if (value.isBlank()) {
            return;
        }
        String[] parts = value.split(",");
        for (String part : parts) {
            String trimmed = normalizeHeaderValue(part);
            if (!trimmed.isBlank()) {
                headers.computeIfAbsent("reward_item", ignored -> new ArrayList<>()).add(trimmed);
            }
        }
    }

    private String getFirst(Map<String, List<String>> headers, String key) {
        List<String> values = headers.get(key);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }

    private List<QuestDefinition.StepDefinition> parseSteps(List<String> stepValues,
                                                            List<String> stepJournals,
                                                            FileHandle questFile) {
        List<QuestDefinition.StepDefinition> steps = new ArrayList<>();
        for (int index = 0; index < stepValues.size(); index++) {
            String value = stepValues.get(index);
            String journal = null;
            if (stepJournals != null && index < stepJournals.size()) {
                journal = normalizeHeaderValue(stepJournals.get(index));
            }
            QuestDefinition.StepDefinition parsed = parseStepDefinition(value, journal, questFile);
            if (parsed != null) {
                steps.add(parsed);
            }
        }
        return steps;
    }

    private QuestDefinition.StepDefinition parseStepDefinition(String value, String journalText, FileHandle questFile) {
        String[] parts = value.trim().split("\\s+");
        if (parts.length == 0) {
            return null;
        }
        String type = parts[0].toLowerCase(Locale.ROOT);
        return switch (type) {
            case "talk" -> {
                if (parts.length < 2) {
                    Gdx.app.error(TAG, "Talk step missing npc: " + questFile.path());
                    yield null;
                }
                yield new QuestDefinition.StepDefinition("talk", parts[1], null, 0, null, journalText);
            }
            case "collect" -> {
                if (parts.length < 2) {
                    Gdx.app.error(TAG, "Collect step missing item: " + questFile.path());
                    yield null;
                }
                if (parts.length == 2) {
                    yield new QuestDefinition.StepDefinition(
                        "collect",
                        null,
                        parts[1],
                        1,
                        null,
                        journalText
                    );
                }
                yield new QuestDefinition.StepDefinition(
                    "collect",
                    null,
                    parts[1],
                    parseInt(parts[2], questFile),
                    null,
                    journalText
                );
            }
            case "kill" -> {
                if (parts.length < 2) {
                    Gdx.app.error(TAG, "Kill step missing npc: " + questFile.path());
                    yield null;
                }
                if (parts.length == 2) {
                    yield new QuestDefinition.StepDefinition(
                        "kill",
                        null,
                        null,
                        1,
                        parts[1],
                        journalText
                    );
                }
                yield new QuestDefinition.StepDefinition(
                    "kill",
                    null,
                    null,
                    parseInt(parts[2], questFile),
                    parts[1],
                    journalText
                );
            }
            default -> {
                Gdx.app.error(TAG, "Unknown step type: " + parts[0]);
                yield null;
            }
        };
    }

    private int parseRewardMoney(Map<String, List<String>> headers, FileHandle questFile) {
        String value = getFirst(headers, "reward_money");
        if (value == null || value.isBlank()) {
            return 0;
        }
        return parseInt(value, questFile);
    }

    private int parseRewardExp(Map<String, List<String>> headers, FileHandle questFile) {
        String value = getFirst(headers, "reward_exp");
        if (value == null || value.isBlank()) {
            return 0;
        }
        return parseInt(value, questFile);
    }

    private List<String> parseRewardItems(Map<String, List<String>> headers) {
        List<String> values = headers.getOrDefault("reward_item", List.of());
        return values.stream()
            .map(this::normalizeHeaderValue)
            .filter(value -> !value.isBlank())
            .toList();
    }

    private RewardTiming parseRewardTiming(Map<String, List<String>> headers, FileHandle questFile) {
        String value = getFirst(headers, "reward_timing");
        if (value == null || value.isBlank()) {
            return RewardTiming.GIVER;
        }
        String normalized = normalizeHeaderValue(value).toUpperCase(Locale.ROOT);
        try {
            return RewardTiming.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            Gdx.app.error(TAG, "Invalid reward_timing in quest file: " + questFile.path() + " (" + value + ")");
            return RewardTiming.GIVER;
        }
    }

    private int parseInt(String value, FileHandle questFile) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            Gdx.app.error(TAG, "Invalid number in quest file: " + questFile.path() + " (" + value + ")");
            return 0;
        }
    }

    private List<String> readYarnHeaders(FileHandle questFile) {
        List<String> lines = List.of(questFile.readString().split("\\r?\\n"));
        List<String> headers = new ArrayList<>();
        for (String line : lines) {
            if (line.trim().equals(YARN_HEADER_TERMINATOR)) {
                break;
            }
            if (!line.trim().isEmpty()) {
                headers.add(line);
            }
        }
        return headers;
    }

    private String normalizeHeaderValue(String value) {
        String trimmed = value.trim();
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
            || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            return trimmed.substring(1, trimmed.length() - 1).trim();
        }
        return trimmed;
    }
}
