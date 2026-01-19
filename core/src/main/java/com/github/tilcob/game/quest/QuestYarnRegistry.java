package com.github.tilcob.game.quest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestYarnRegistry {
    private static final String TAG = QuestYarnRegistry.class.getSimpleName();
    private static final String QUESTS_DIR = "quests";
    private static final String YARN_HEADER_TERMINATOR = "---";
    private static final Pattern HEADER_PATTERN = Pattern.compile("^([A-Za-z0-9_.-]+)\\s*:\\s*(.*)$");

    private final String indexPath;
    private final Map<String, QuestDefinition> definitions = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public QuestYarnRegistry(String indexPath) {
        this.indexPath = indexPath;
    }

    public Map<String, QuestDefinition> loadAll() {
        definitions.clear();
        FileHandle indexFile = Gdx.files.internal(indexPath);
        if (!indexFile.exists()) {
            Gdx.app.error(TAG, "Quest index not found: " + indexPath);
            return Collections.unmodifiableMap(definitions);
        }

        try {
            JsonNode root = mapper.readTree(indexFile.readString());
            if (!root.isArray()) {
                Gdx.app.error(TAG, "Quest index must be a JSON array: " + indexFile.path());
                return Collections.unmodifiableMap(definitions);
            }
            for (JsonNode entry : root) {
                if (!entry.isTextual()) {
                    Gdx.app.error(TAG, "Invalid quest index entry: " + entry.toString());
                    continue;
                }
                loadQuestDefinition(resolveQuestFileFromEntry(entry.asText()));
            }
        } catch (IOException e) {
            Gdx.app.error(TAG, "Failed to read quest index: " + indexFile.path(), e);
        }
        return Collections.unmodifiableMap(definitions);
    }

    public QuestDefinition getQuestDefinition(String questId) {
        return definitions.get(questId);
    }

    public boolean isEmpty() {
        return definitions.isEmpty();
    }

    private void loadQuestDefinition(FileHandle questFile) {
        if (!questFile.exists()) {
            Gdx.app.error(TAG, "Quest file not found: " + questFile.path());
            return;
        }
        QuestDefinition quest = parseQuestDefinitionFromYarn(questFile);
        if (quest != null) definitions.put(quest.questId(), quest);
    }

    private QuestDefinition parseQuestDefinitionFromYarn(FileHandle questFile) {
        List<String> headerLines = readYarnHeaders(questFile);
        if (headerLines.isEmpty()) {
            Gdx.app.error(TAG, "Quest file missing Yarn headers: " + questFile.path());
            return null;
        }

        String questId = null;
        String displayName = null;
        String journalText = null;
        String startNode = null;
        List<QuestDefinition.StepDefinition> steps = new ArrayList<>();
        QuestDefinition.RewardDefinition reward = new QuestDefinition.RewardDefinition(0, List.of());
        int rewardMoney = 0;
        List<String> rewardItems = new ArrayList<>();

        for (String line : headerLines) {
            Matcher matcher = HEADER_PATTERN.matcher(line);
            if (!matcher.matches()) {
                continue;
            }
            String key = matcher.group(1);
            String value = matcher.group(2).trim();
            switch (key) {
                case "questId" -> questId = normalizeHeaderValue(value);
                case "displayName" -> displayName = normalizeHeaderValue(value);
                case "journalText" -> journalText = normalizeHeaderValue(value);
                case "startNode" -> startNode = normalizeHeaderValue(value);
                case "step" -> {
                    QuestDefinition.StepDefinition parsed = parseStepDefinition(value, questFile);
                    if (parsed != null) {
                        steps.add(parsed);
                    }
                }
                case "reward.money" -> rewardMoney = parseInt(value, questFile);
                case "reward.item" -> {
                    if (!value.isBlank()) {
                        rewardItems.add(normalizeHeaderValue(value));
                    }
                }
                default -> {
                }
            }
        }

        if (questId == null || questId.isBlank()) {
            Gdx.app.error(TAG, "Quest entry missing questId: " + questFile.path());
            return null;
        }
        if (definitions.containsKey(questId)) {
            Gdx.app.error(TAG, "Duplicate questId detected: " + questId);
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
        if (startNode == null || startNode.isBlank()) {
            startNode = "q_" + questId + "_start";
        }
        if (!rewardItems.isEmpty() || rewardMoney != 0) {
            reward = new QuestDefinition.RewardDefinition(rewardMoney, List.copyOf(rewardItems));
        }
        return new QuestDefinition(
            questId,
            displayName,
            journalText,
            startNode,
            List.copyOf(steps),
            reward
        );
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

    private QuestDefinition.StepDefinition parseStepDefinition(String value, FileHandle questFile) {
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
                yield new QuestDefinition.StepDefinition("talk", parts[1], null, 0, null);
            }
            case "collect" -> {
                if (parts.length < 3) {
                    Gdx.app.error(TAG, "Collect step missing args: " + questFile.path());
                    yield null;
                }
                yield new QuestDefinition.StepDefinition("collect", null, parts[1], parseInt(parts[2], questFile), null);
            }
            case "kill" -> {
                if (parts.length < 3) {
                    Gdx.app.error(TAG, "Kill step missing args: " + questFile.path());
                    yield null;
                }
                yield new QuestDefinition.StepDefinition("kill", null, null, parseInt(parts[2], questFile), parts[1]);
            }
            default -> {
                Gdx.app.error(TAG, "Unknown step type: " + parts[0]);
                yield null;
            }
        };
    }

    private int parseInt(String value, FileHandle questFile) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Gdx.app.error(TAG, "Invalid number in quest file: " + questFile.path() + " (" + value + ")");
            return 0;
        }
    }

    private String normalizeHeaderValue(String value) {
        String trimmed = value.trim();
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
            || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            return trimmed.substring(1, trimmed.length() - 1).trim();
        }
        return trimmed;
    }

    private FileHandle resolveQuestFileFromEntry(String entry) {
        String fileName = ensureYarnExtension(entry);
        if (fileName.contains("/")) {
            return Gdx.files.internal(fileName);
        }
        return Gdx.files.internal(QUESTS_DIR + "/" + fileName);
    }

    private String ensureYarnExtension(String entry) {
        if (entry.toLowerCase().endsWith(".yarn")) {
            return entry;
        }
        return entry + ".yarn";
    }
}
